const KAKAO_APP_KEY = '95594389d96723d3b307c14b21a9eeef';

let kakaoMapsLoader;
let cachedGeocoder = null;

export const loadKakaoMaps = () => {
  if (typeof window === 'undefined') {
    return Promise.reject(
      new Error('Kakao Maps can only be loaded in the browser.')
    );
  }

  if (window.kakao && window.kakao.maps) {
    return Promise.resolve(window.kakao.maps);
  }

  if (!kakaoMapsLoader) {
    kakaoMapsLoader = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${KAKAO_APP_KEY}&autoload=false&libraries=services`;
      script.async = true;
      script.onerror = () => reject(new Error('카카오 지도 SDK 로딩 실패'));
      script.onload = () => {
        window.kakao.maps.load(() => {
          resolve(window.kakao.maps);
        });
      };
      document.head.appendChild(script);
    });
  }

  return kakaoMapsLoader;
};

const getGeocoder = async () => {
  const maps = await loadKakaoMaps();
  if (!cachedGeocoder) {
    cachedGeocoder = new maps.services.Geocoder();
  }
  return { maps, geocoder: cachedGeocoder };
};

export const geocodeAddress = async (address) => {
  if (!address) {
    throw new Error('주소가 필요합니다.');
  }

  const { maps, geocoder } = await getGeocoder();

  return new Promise((resolve, reject) => {
    geocoder.addressSearch(address, (result, status) => {
      if (status === maps.services.Status.OK && result?.length) {
        const { x, y } = result[0];
        resolve({
          lat: parseFloat(y),
          lng: parseFloat(x),
        });
      } else {
        reject(new Error(`주소(${address})를 좌표로 변환할 수 없습니다.`));
      }
    });
  });
};
