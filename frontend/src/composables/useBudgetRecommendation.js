import { ref } from "vue";
import { restaurants as restaurantData } from "@/data/restaurants";

export const extractPriceValue = (priceText = "") => {
  const match = priceText.match(/([\d.,]+)/);
  const target = match?.[1] ?? priceText;
  const digits = String(target).replace(/[^0-9]/g, "");
  if (!digits) return null;
  return Number(digits);
};

const filterByBudget = (perPersonBudget) => {
  const budget = Number(perPersonBudget);
  if (!Number.isFinite(budget) || budget <= 0) {
    return [];
  }
  return restaurantData.filter((restaurant) => {
    const priceValue = extractPriceValue(restaurant?.price ?? "");
    if (priceValue == null) return false;
    if(budget >= 500000) return priceValue >= Number(500000*0.9); //상한
    if(budget <= 10000) return priceValue <= Number(10000*1.1); //하한
    return (priceValue <= Number(budget*1.1)) && (priceValue >= Number(budget*0.9));
  });
};

export function useBudgetRecommendation() {
  const budgetRecommendations = ref([]);

  const fetchBudgetRecommendations = (perPersonBudget) => {
    budgetRecommendations.value = filterByBudget(perPersonBudget);
  };

  const clearBudgetRecommendations = () => {
    budgetRecommendations.value = [];
  };

  return {
    budgetRecommendations,
    fetchBudgetRecommendations,
    clearBudgetRecommendations,
  };
}
