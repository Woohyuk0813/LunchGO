export const useHomeRecommendations = ({
  selectedSort,
  selectedPriceRange,
  selectedRecommendation,
  filterForm,
  sortOptions,
  filterPerPersonBudget,
  fetchBudgetRecommendations,
  clearBudgetRecommendations,
  fetchWeatherRecommendations,
  clearWeatherRecommendations,
  fetchTagMappingRecommendations,
  clearTagMappingRecommendations,
  clearTrendingRestaurants,
  cafeteriaRecommendations,
  clearCafeteriaRecommendations,
  resolveCafeteriaBaseDate,
  checkCafeteriaMenuStatus,
  openCafeteriaModal,
  requestCafeteriaRecommendations,
  hasConfirmedMenus,
  currentPage,
  isFilterOpen,
  persistHomeListState,
  RECOMMEND_CAFETERIA,
  RECOMMEND_BUDGET,
  RECOMMEND_TASTE,
  RECOMMEND_WEATHER,
  RECOMMEND_TRENDING,
}) => {
  const applyFilters = () => {
    selectedSort.value = filterForm.sort || sortOptions[0];
    selectedPriceRange.value = filterForm.priceRange || null;
    selectedRecommendation.value = filterForm.recommendation || null;
    if (selectedRecommendation.value === RECOMMEND_BUDGET) {
      fetchBudgetRecommendations(filterPerPersonBudget.value);
    } else {
      clearBudgetRecommendations();
    }
    if (selectedRecommendation.value === RECOMMEND_WEATHER) {
      fetchWeatherRecommendations();
    } else {
      clearWeatherRecommendations();
    }
    if (selectedRecommendation.value === RECOMMEND_TASTE) {
      fetchTagMappingRecommendations();
    } else {
      clearTagMappingRecommendations();
    }
    currentPage.value = 1;
    isFilterOpen.value = false;
    persistHomeListState();
  };

  const clearTrendingRecommendation = () => {
    selectedRecommendation.value = null;
    filterForm.recommendation = null;
    clearTrendingRestaurants();
    currentPage.value = 1;
    persistHomeListState();
  };

  const toggleRecommendationOption = (option) => {
    if (option === RECOMMEND_CAFETERIA) {
      filterForm.recommendation = option;
      const baseDate = resolveCafeteriaBaseDate();
      checkCafeteriaMenuStatus(baseDate);
      return;
    }

    filterForm.recommendation =
      filterForm.recommendation === option ? null : option;
  };

  const handleRecommendationQuickSelect = async (option) => {
    if (option === RECOMMEND_CAFETERIA) {
      selectedRecommendation.value = option;
      filterForm.recommendation = option;
      const baseDate = resolveCafeteriaBaseDate();
      await checkCafeteriaMenuStatus(baseDate);
      if (hasConfirmedMenus.value) {
        await requestCafeteriaRecommendations(baseDate);
      } else if (openCafeteriaModal) {
        openCafeteriaModal();
      }
      return;
    }
    if (cafeteriaRecommendations.value.length) {
      clearCafeteriaRecommendations();
    }
    toggleRecommendationOption(option);
    applyFilters();
  };

  const clearRecommendation = (option) => {
    if (option === RECOMMEND_CAFETERIA) {
      clearCafeteriaRecommendations();
    }
    if (option === RECOMMEND_BUDGET) {
      clearBudgetRecommendations();
    }
    if (option === RECOMMEND_WEATHER) {
      clearWeatherRecommendations();
    }
    if (option === RECOMMEND_TASTE) {
      clearTagMappingRecommendations();
    }
    if (option === RECOMMEND_TRENDING) {
      clearTrendingRestaurants();
    }
    if (selectedRecommendation.value === option) {
      selectedRecommendation.value = null;
      filterForm.recommendation = null;
    }
    currentPage.value = 1;
    persistHomeListState();
  };

  return {
    applyFilters,
    clearTrendingRecommendation,
    toggleRecommendationOption,
    handleRecommendationQuickSelect,
    clearRecommendation,
  };
};
