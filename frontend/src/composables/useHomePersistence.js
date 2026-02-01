export const useHomePersistence = (options = {}) => {
  const storageKey = options.storageKey || "homeListState";

  const loadHomeListState = () => {
    const raw = sessionStorage.getItem(storageKey);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      sessionStorage.removeItem(storageKey);
      return null;
    }
  };

  const saveHomeListState = (state) => {
    sessionStorage.setItem(storageKey, JSON.stringify(state));
  };

  const clearHomeListState = () => {
    sessionStorage.removeItem(storageKey);
  };

  return {
    loadHomeListState,
    saveHomeListState,
    clearHomeListState,
  };
};
