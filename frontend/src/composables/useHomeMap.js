import { ref, computed, watch, nextTick, onBeforeUnmount } from "vue";
import { loadKakaoMaps } from "@/utils/kakao";
import {
  buildMarkerBody,
  buildMarkerCircle,
  buildMarkerDataUri,
  buildMarkerStar,
} from "@/utils/mapMarkerSvgs";

export const useHomeMap = ({
  isLoggedIn,
  fetchUserAddress,
  geocodeAddress,
  isSearchOpen,
  selectedDistanceKm,
  resolveRestaurantCoords,
  onMarkerClick,
  favoriteIdSet,
  sharedFavoriteIdSet,
  sharedFavoriteNameMap,
  restaurants,
}) => {
  const mapContainer = ref(null);
  const mapInstance = ref(null);
  const kakaoMapsApi = ref(null);
  const isMapReady = ref(false);
  const markerRegistry = new Map();
  const overlayRegistry = new Map();
  const isMapInteracting = ref(false);
  const needsMarkerRender = ref(false);
  const fallbackMapCenter = {
    lat: 37.394374,
    lng: 127.110636,
  };
  const fallbackAddress = "경기도 성남시 분당구 판교역로 235";
  const mapCenter = ref({ ...fallbackMapCenter });
  const currentLocation = ref(fallbackAddress);
  const routePolyline = ref(null);
  const routeFocus = ref(null);
  const routeOriginMarker = ref(null);
  const routeFocusMarker = ref(null);
  const selectedMarkerKey = ref(null);
  const mapDistanceSteps = Object.freeze([
    { label: "100m", level: 2 },
    { label: "250m", level: 3 },
    { label: "500m", level: 4 },
    { label: "1km", level: 5 },
    { label: "2km", level: 6 },
    { label: "3km", level: 7 },
  ]);
  const defaultMapDistanceIndex = mapDistanceSteps.findIndex(
    (step) => step.label === "500m"
  );
  const mapDistanceStepIndex = ref(
    defaultMapDistanceIndex === -1 ? 0 : defaultMapDistanceIndex
  );
  const currentDistanceLabel = computed(
    () => mapDistanceSteps[mapDistanceStepIndex.value].label
  );
  const distanceSliderFill = computed(() => {
    if (mapDistanceSteps.length <= 1) return 0;
    return (
      ((mapDistanceSteps.length - 1 - mapDistanceStepIndex.value) /
        (mapDistanceSteps.length - 1)) *
      100
    );
  });

  const getRestaurants = () =>
    typeof restaurants === "function" ? restaurants() : restaurants || [];

  const getRestaurantKey = (restaurant) => {
    const id =
      restaurant?.id ?? restaurant?.restaurantId ?? restaurant?.restaurant_id;
    if (id !== undefined && id !== null && id !== "") {
      return String(id);
    }
    const name = restaurant?.name ?? "unknown";
    const address =
      restaurant?.address ?? restaurant?.roadAddress ?? restaurant?.location ?? "";
    return `${name}-${address}`;
  };

  const haversineDistance = (coordsA = {}, coordsB = {}) => {
    if (!coordsA.lat || !coordsA.lng || !coordsB.lat || !coordsB.lng) {
      return Number.POSITIVE_INFINITY;
    }
    const toRad = (value) => (value * Math.PI) / 180;
    const earthRadius = 6371;
    const dLat = toRad(coordsB.lat - coordsA.lat);
    const dLng = toRad(coordsB.lng - coordsA.lng);
    const lat1 = toRad(coordsA.lat);
    const lat2 = toRad(coordsB.lat);

    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(lat1) * Math.cos(lat2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return earthRadius * c;
  };

  const isValidCoords = (coords) =>
    Number.isFinite(coords?.lat) && Number.isFinite(coords?.lng);

  const isWithinDistance = (coords, limitKm) => {
    if (!limitKm) return true;
    if (!isValidCoords(coords)) return false;
    return haversineDistance(coords, mapCenter.value) <= limitKm;
  };

  const setMarkerVisible = (marker, visible) => {
    if (!marker) return;
    if (typeof marker.setVisible === "function") {
      marker.setVisible(visible);
      return;
    }
    if (!visible) {
      marker.setMap(null);
    }
  };
  const setOverlayVisible = (overlay, visible) => {
    if (!overlay) return;
    if (visible) {
      overlay.setMap(mapInstance.value);
      return;
    }
    overlay.setMap(null);
  };

  const renderMapMarkers = async (kakaoMaps, shouldClear = true) => {
    if (!mapInstance.value) return;
    if (isMapInteracting.value) return;

    const selectedMarkerImage = new kakaoMaps.MarkerImage(
      "data:image/svg+xml;utf8," +
        "<svg xmlns='http://www.w3.org/2000/svg' width='34' height='50' viewBox='0 0 34 50'>" +
        "<path d='M17 1C9.4 1 3 7.4 3 15.1c0 10.1 14 32.9 14 32.9s14-22.8 14-32.9C31 7.4 24.6 1 17 1z' fill='%231e3a5f' stroke='%23ffffff' stroke-width='2'/>" +
        "<circle cx='17' cy='15' r='5.5' fill='white'/>" +
        "</svg>",
      new kakaoMaps.Size(34, 50),
      { offset: new kakaoMaps.Point(17, 50) }
    );
    if (shouldClear) {
      markerRegistry.forEach((entry) => entry.marker.setMap(null));
      markerRegistry.clear();
      overlayRegistry.forEach((overlay) => overlay.setMap(null));
      overlayRegistry.clear();
      if (routeOriginMarker.value) {
        routeOriginMarker.value.setMap(null);
        routeOriginMarker.value = null;
      }
      if (routeFocusMarker.value) {
        routeFocusMarker.value.setMap(null);
        routeFocusMarker.value = null;
      }
    }

    const markerSvg = buildMarkerDataUri(
      buildMarkerBody("#ff6b4a", buildMarkerCircle("white"))
    );
    const favoriteMarkerSvg = buildMarkerDataUri(
      buildMarkerBody("#007bff", buildMarkerStar("white"))
    );
    const sharedMarkerSvg = buildMarkerDataUri(
      buildMarkerBody("#ffc107", buildMarkerStar("white"))
    );
    const sharedFavoriteMarkerSvg = buildMarkerDataUri(
      buildMarkerBody("#007bff", buildMarkerStar("#ffc107"))
    );
    const markerImage = new kakaoMaps.MarkerImage(
      markerSvg,
      new kakaoMaps.Size(32, 46),
      { offset: new kakaoMaps.Point(16, 46) }
    );
    const favoriteMarkerImage = new kakaoMaps.MarkerImage(
      favoriteMarkerSvg,
      new kakaoMaps.Size(32, 46),
      { offset: new kakaoMaps.Point(16, 46) }
    );
    const sharedMarkerImage = new kakaoMaps.MarkerImage(
      sharedMarkerSvg,
      new kakaoMaps.Size(32, 46),
      { offset: new kakaoMaps.Point(16, 46) }
    );
    const sharedFavoriteMarkerImage = new kakaoMaps.MarkerImage(
      sharedFavoriteMarkerSvg,
      new kakaoMaps.Size(32, 46),
      { offset: new kakaoMaps.Point(16, 46) }
    );
    const originMarkerSvg = buildMarkerDataUri(
      buildMarkerBody("#1e3a5f", buildMarkerCircle("white"))
    );
    const originMarkerImage = new kakaoMaps.MarkerImage(
      originMarkerSvg,
      new kakaoMaps.Size(32, 46),
      { offset: new kakaoMaps.Point(16, 46) }
    );

    const distanceLimit = selectedDistanceKm.value;
    const favoriteIds = favoriteIdSet?.value ?? new Set();
    const sharedFavoriteIds = sharedFavoriteIdSet?.value ?? new Set();
    const sharedNameLookup = sharedFavoriteNameMap?.value || {};
    const resolveGroupFlags = (groupRestaurants = []) => {
      const hasFavorite = groupRestaurants.some((item) =>
        favoriteIds.has(Number(item.id ?? item.restaurantId))
      );
      const hasSharedFavorite = groupRestaurants.some((item) =>
        sharedFavoriteIds.has(Number(item.id ?? item.restaurantId))
      );
      return { hasFavorite, hasSharedFavorite };
    };
    const resolveSharedLabel = (groupRestaurants = []) => {
      const names = new Set();
      groupRestaurants.forEach((item) => {
        const id = Number(item.id ?? item.restaurantId);
        const entry = sharedNameLookup?.[id];
        if (!Array.isArray(entry)) return;
        entry.forEach((name) => {
          if (name) names.add(String(name));
        });
      });
      const list = Array.from(names);
      if (!list.length) return "";
      if (list.length === 1) return list[0];
      return `${list[0]} 외 ${list.length - 1}`;
    };
    const applyMarkerStyle = (key, marker, flags) => {
      if (!marker) return;
      if (key && selectedMarkerKey.value === key) {
        marker.setImage(selectedMarkerImage);
        return;
      }
      if (flags?.hasFavorite && flags?.hasSharedFavorite) {
        marker.setImage(sharedFavoriteMarkerImage);
        return;
      }
      if (flags?.hasFavorite) {
        marker.setImage(favoriteMarkerImage);
        return;
      }
      if (flags?.hasSharedFavorite) {
        marker.setImage(sharedMarkerImage);
        return;
      }
      marker.setImage(markerImage);
    };

    if (routeFocus.value && isValidCoords(routeFocus.value)) {
      markerRegistry.forEach((entry) => setMarkerVisible(entry.marker, false));
      overlayRegistry.forEach((overlay) => setOverlayVisible(overlay, false));
      if (routeFocusMarker.value) {
        routeFocusMarker.value.setMap(null);
        routeFocusMarker.value = null;
      }
      if (isValidCoords(mapCenter.value)) {
        const originMarker = new kakaoMaps.Marker({
          position: new kakaoMaps.LatLng(mapCenter.value.lat, mapCenter.value.lng),
          title: "회사",
          image: originMarkerImage,
        });
        originMarker.setMap(mapInstance.value);
        routeOriginMarker.value = originMarker;
      }

      const focusMarker = new kakaoMaps.Marker({
        position: new kakaoMaps.LatLng(routeFocus.value.lat, routeFocus.value.lng),
        title: routeFocus.value.name || "식당",
        image: markerImage,
      });
      focusMarker.setMap(mapInstance.value);
      routeFocusMarker.value = focusMarker;
      return;
    }

    const markerGroups = new Map();
    for (const restaurant of getRestaurants()) {
      const coords = await resolveRestaurantCoords(restaurant);
      if (!isValidCoords(coords)) continue;
      if (distanceLimit && !isWithinDistance(coords, distanceLimit)) {
        continue;
      }
      const coordKey = `${coords.lat.toFixed(6)},${coords.lng.toFixed(6)}`;
      const group = markerGroups.get(coordKey) ?? {
        coords,
        restaurants: [],
      };
      group.restaurants.push(restaurant);
      group.coords = coords;
      markerGroups.set(coordKey, group);
    }

    const nextKeys = new Set(markerGroups.keys());
    markerGroups.forEach((group, key) => {
      const coords = group.coords;
      const flags = resolveGroupFlags(group.restaurants);
      const labelText = flags.hasSharedFavorite
        ? resolveSharedLabel(group.restaurants)
        : "";
      const existing = markerRegistry.get(key);
      try {
        if (existing) {
          existing.restaurants = group.restaurants;
          if (existing.coordsKey !== key) {
            existing.coordsKey = key;
          }
          if (coords && isValidCoords(coords)) {
            existing.marker.setPosition(
              new kakaoMaps.LatLng(coords.lat, coords.lng)
            );
          }
          if (typeof existing.marker.setVisible === "function") {
            existing.marker.setVisible(true);
          } else if (!existing.marker.getMap()) {
            existing.marker.setMap(mapInstance.value);
          }
          applyMarkerStyle(key, existing.marker, flags);
        } else {
          const marker = new kakaoMaps.Marker({
            position: new kakaoMaps.LatLng(coords.lat, coords.lng),
            title: group.restaurants[0]?.name ?? "",
            image: markerImage,
          });
          marker.setMap(mapInstance.value);
          if (onMarkerClick) {
            const clickHandler = () => {
              const entry = markerRegistry.get(key);
              if (entry?.restaurants?.length) {
                selectedMarkerKey.value = key;
                markerRegistry.forEach((value, entryKey) => {
                  const entryFlags = resolveGroupFlags(value.restaurants);
                  applyMarkerStyle(entryKey, value.marker, entryFlags);
                });
                onMarkerClick(entry.restaurants);
              }
            };
            kakaoMaps.event.addListener(marker, "click", clickHandler);
            markerRegistry.set(key, {
              marker,
              restaurants: group.restaurants,
              coordsKey: key,
              clickHandler,
            });
          } else {
            markerRegistry.set(key, {
              marker,
              restaurants: group.restaurants,
              coordsKey: key,
            });
          }
          applyMarkerStyle(key, marker, flags);
        }

        if (flags.hasSharedFavorite && labelText) {
          const overlayContent = `<div style="transform: translate(14px, -36px); background: #ffc107; color: #1e3a5f; font-size: 10px; font-weight: 600; padding: 2px 6px; border-radius: 999px; border: 1px solid #ffffff; box-shadow: 0 2px 6px rgba(0,0,0,0.15); white-space: nowrap; pointer-events: none;">${labelText}</div>`;
          const existingOverlay = overlayRegistry.get(key);
          if (existingOverlay) {
            existingOverlay.setContent(overlayContent);
            existingOverlay.setPosition(
              new kakaoMaps.LatLng(coords.lat, coords.lng)
            );
            setOverlayVisible(existingOverlay, true);
          } else {
            const overlay = new kakaoMaps.CustomOverlay({
              position: new kakaoMaps.LatLng(coords.lat, coords.lng),
              content: overlayContent,
              yAnchor: 1,
              xAnchor: 0,
            });
            overlay.setMap(mapInstance.value);
            overlayRegistry.set(key, overlay);
          }
        } else if (overlayRegistry.has(key)) {
          const overlay = overlayRegistry.get(key);
          setOverlayVisible(overlay, false);
          overlayRegistry.delete(key);
        }
      } catch (error) {
        console.error("지도 마커 표시 실패:", group?.restaurants?.[0]?.name, error);
      }
    });

    markerRegistry.forEach((entry, key) => {
      if (!nextKeys.has(key)) {
        entry.marker.setMap(null);
        markerRegistry.delete(key);
      }
    });
    overlayRegistry.forEach((overlay, key) => {
      if (!nextKeys.has(key)) {
        overlay.setMap(null);
        overlayRegistry.delete(key);
      }
    });
  };

  const levelForDistance = (stepIndex) => {
    const step = mapDistanceSteps[stepIndex] ?? mapDistanceSteps[0];
    return step.level;
  };

  let mapZoomRafId = 0;
  let mapZoomRetryId = 0;
  let mapZoomAttempts = 0;
  let pendingMapZoomLevel = null;
  const maxMapZoomAttempts = 3;
  const scheduleMapZoom = (force = false) => {
    if (isMapInteracting.value) {
      return;
    }
    if (mapZoomRafId) {
      cancelAnimationFrame(mapZoomRafId);
    }
    if (mapZoomRetryId) {
      clearTimeout(mapZoomRetryId);
      mapZoomRetryId = 0;
    }
    mapZoomAttempts = 0;
    mapZoomRafId = requestAnimationFrame(() => {
      mapZoomRafId = 0;
      if (!mapInstance.value) return;
      if (isMapInteracting.value) return;
      if (!force && !isMapReady.value) return;
      if (isSearchOpen.value) return;
      if (!mapContainer.value?.offsetWidth || !mapContainer.value?.offsetHeight) {
        return;
      }
      if (!mapContainer.value?.isConnected || !mapContainer.value?.offsetParent) {
        return;
      }
      const targetLevel =
        pendingMapZoomLevel ?? levelForDistance(mapDistanceStepIndex.value);
      const attemptZoom = () => {
        if (!mapInstance.value) return;
        try {
          if (typeof mapInstance.value.getLevel === "function") {
            const currentLevel = mapInstance.value.getLevel();
            if (currentLevel === targetLevel) {
              pendingMapZoomLevel = null;
              return;
            }
          }
          mapInstance.value.setLevel(targetLevel);
          pendingMapZoomLevel = null;
        } catch (error) {
          mapZoomAttempts += 1;
          if (mapZoomAttempts < maxMapZoomAttempts) {
            mapZoomRetryId = setTimeout(attemptZoom, 120);
          }
        }
      };
      attemptZoom();
    });
  };

  const applyHomeMapZoom = (force = false) => {
    pendingMapZoomLevel = levelForDistance(mapDistanceStepIndex.value);
    scheduleMapZoom(force);
  };

  const changeMapDistance = (delta) => {
    const next = Math.min(
      mapDistanceSteps.length - 1,
      Math.max(0, mapDistanceStepIndex.value + delta)
    );
    mapDistanceStepIndex.value = next;
  };

  const resetMapToHome = () => {
    if (!mapInstance.value) return;
    const kakaoMaps = window?.kakao?.maps;
    if (!kakaoMaps?.LatLng) return;

    const targetCenter = new kakaoMaps.LatLng(
      mapCenter.value.lat,
      mapCenter.value.lng
    );
    mapInstance.value.panTo(targetCenter);
    mapDistanceStepIndex.value =
      defaultMapDistanceIndex === -1 ? 0 : defaultMapDistanceIndex;
    applyHomeMapZoom();
  };

  const initializeMap = async () => {
    if (!mapContainer.value) return;
    try {
      const kakaoMaps = await loadKakaoMaps();
      kakaoMapsApi.value = kakaoMaps;
      const center = new kakaoMaps.LatLng(
        mapCenter.value.lat,
        mapCenter.value.lng
      );
      mapInstance.value = new kakaoMaps.Map(mapContainer.value, {
        center,
        level: levelForDistance(mapDistanceStepIndex.value),
      });
      if (typeof mapInstance.value.setDraggable === "function") {
        mapInstance.value.setDraggable(true);
      }
      kakaoMaps.event.addListener(mapInstance.value, "zoom_start", () => {
        isMapInteracting.value = true;
      });
      kakaoMaps.event.addListener(mapInstance.value, "zoom_end", () => {
        isMapInteracting.value = false;
        if (needsMarkerRender.value) {
          scheduleMapMarkerRender();
        }
      });
      kakaoMaps.event.addListener(mapInstance.value, "dragstart", () => {
        isMapInteracting.value = true;
      });
      kakaoMaps.event.addListener(mapInstance.value, "dragend", () => {
        isMapInteracting.value = false;
        if (needsMarkerRender.value) {
          scheduleMapMarkerRender();
        }
      });
      kakaoMaps.event.addListener(mapInstance.value, "idle", () => {
        isMapInteracting.value = false;
        if (needsMarkerRender.value) {
          scheduleMapMarkerRender();
        }
      });
      await renderMapMarkers(kakaoMaps);
      applyHomeMapZoom(true);
      isMapReady.value = true;
    } catch (error) {
      console.error("카카오 지도 초기화에 실패했습니다.", error);
    }
  };

  let mapRenderRafId = 0;
  const scheduleMapMarkerRender = () => {
    if (mapRenderRafId) {
      cancelAnimationFrame(mapRenderRafId);
    }
    needsMarkerRender.value = true;
    if (isMapInteracting.value) {
      return;
    }
    mapRenderRafId = requestAnimationFrame(() => {
      mapRenderRafId = 0;
      if (!isMapReady.value || !kakaoMapsApi.value || !mapInstance.value) {
        return;
      }
      if (!mapContainer.value?.offsetWidth || !mapContainer.value?.offsetHeight) {
        return;
      }
      renderMapMarkers(kakaoMapsApi.value, false);
      needsMarkerRender.value = false;
    });
  };

  const applyUserMapCenter = async () => {
    if (!isLoggedIn.value) return;
    const address = await fetchUserAddress();
    if (!address) {
      currentLocation.value = fallbackAddress;
      mapCenter.value = { ...fallbackMapCenter };
      return;
    }
    currentLocation.value = address;
    try {
      const coords = await geocodeAddress(address);
      mapCenter.value = coords;
      if (mapInstance.value && kakaoMapsApi.value?.LatLng) {
        const center = new kakaoMapsApi.value.LatLng(coords.lat, coords.lng);
        mapInstance.value.setCenter(center);
        scheduleMapMarkerRender();
      }
    } catch (error) {
      // keep fallback center if geocode fails
    }
  };

  watch(mapDistanceStepIndex, () => {
    applyHomeMapZoom();
  });

  watch(isSearchOpen, (isOpen) => {
    if (!isOpen) {
      nextTick(() => {
        setTimeout(() => {
          applyHomeMapZoom(true);
        }, 120);
      });
    }
  });

  watch(selectedDistanceKm, (distanceLimit) => {
    if (distanceLimit) {
      const label = `${distanceLimit}km`;
      const stepIndex = mapDistanceSteps.findIndex(
        (step) => step.label === label
      );
      if (stepIndex !== -1) {
        mapDistanceStepIndex.value = stepIndex;
      }
    }

    if (!routeFocus.value && kakaoMapsApi.value) {
      renderMapMarkers(kakaoMapsApi.value);
    }
  });

  watch(
    () => favoriteIdSet?.value,
    () => {
      scheduleMapMarkerRender();
    }
  );
  watch(
    () => sharedFavoriteIdSet?.value,
    () => {
      scheduleMapMarkerRender();
    }
  );
  watch(
    () => sharedFavoriteNameMap?.value,
    () => {
      scheduleMapMarkerRender();
    }
  );

  onBeforeUnmount(() => {
    markerRegistry.forEach((entry) => entry.marker.setMap(null));
    markerRegistry.clear();
    overlayRegistry.forEach((overlay) => overlay.setMap(null));
    overlayRegistry.clear();
    mapInstance.value = null;
    isMapReady.value = false;
    if (routePolyline.value) {
      routePolyline.value.setMap(null);
      routePolyline.value = null;
    }
    if (routeOriginMarker.value) {
      routeOriginMarker.value.setMap(null);
      routeOriginMarker.value = null;
    }
    if (routeFocusMarker.value) {
      routeFocusMarker.value.setMap(null);
      routeFocusMarker.value = null;
    }
    routeFocus.value = null;
    if (mapRenderRafId) {
      cancelAnimationFrame(mapRenderRafId);
      mapRenderRafId = 0;
    }
    if (mapZoomRafId) {
      cancelAnimationFrame(mapZoomRafId);
      mapZoomRafId = 0;
    }
    if (mapZoomRetryId) {
      clearTimeout(mapZoomRetryId);
      mapZoomRetryId = 0;
    }
  });

  const clearRoute = () => {
    if (routePolyline.value) {
      routePolyline.value.setMap(null);
      routePolyline.value = null;
    }
  };

  const setRouteFocus = (coords, name) => {
    if (!coords) {
      routeFocus.value = null;
      scheduleMapMarkerRender();
      return;
    }
    const normalized = {
      lat: Number(coords.lat),
      lng: Number(coords.lng),
    };
    if (!isValidCoords(normalized)) {
      routeFocus.value = null;
      scheduleMapMarkerRender();
      return;
    }
    routeFocus.value = { ...normalized, name };
    scheduleMapMarkerRender();
  };

  const clearRouteFocus = () => {
    routeFocus.value = null;
    if (routeOriginMarker.value) {
      routeOriginMarker.value.setMap(null);
      routeOriginMarker.value = null;
    }
    if (routeFocusMarker.value) {
      routeFocusMarker.value.setMap(null);
      routeFocusMarker.value = null;
    }
    selectedMarkerKey.value = null;
    scheduleMapMarkerRender();
  };

  const drawRoute = (pathPoints = []) => {
    if (!mapInstance.value || !kakaoMapsApi.value?.LatLng) return;
    clearRoute();
    if (!Array.isArray(pathPoints) || pathPoints.length === 0) return;

    const kakaoMaps = kakaoMapsApi.value;
    const path = pathPoints
      .filter((point) => Number.isFinite(point?.lat) && Number.isFinite(point?.lng))
      .map((point) => new kakaoMaps.LatLng(point.lat, point.lng));
    if (path.length < 2) return;

    const polyline = new kakaoMaps.Polyline({
      path,
      strokeWeight: 6,
      strokeColor: "#d9480f",
      strokeOpacity: 0.95,
      strokeStyle: "solid",
    });
    polyline.setMap(mapInstance.value);
    routePolyline.value = polyline;
  };

  return {
    mapContainer,
    mapCenter,
    currentLocation,
    isMapReady,
    mapDistanceStepIndex,
    currentDistanceLabel,
    distanceSliderFill,
    applyUserMapCenter,
    initializeMap,
    resetMapToHome,
    changeMapDistance,
    applyHomeMapZoom,
    isWithinDistance,
    calculateDistanceKm: haversineDistance,
    drawRoute,
    clearRoute,
    setRouteFocus,
    clearRouteFocus,
  };
};
