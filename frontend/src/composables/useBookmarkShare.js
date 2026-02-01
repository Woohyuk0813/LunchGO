import httpRequest from "@/router/httpRequest";

export const useBookmarkShare = () => {
  const searchUserByEmail = (email) =>
    httpRequest.get("/api/bookmark-links/search", { email });

  const searchUsersByEmail = (query) =>
    httpRequest.get("/api/bookmark-links/search/list", { query });

  const requestLink = (requesterId, receiverId) =>
    httpRequest.post("/api/bookmark-links", { requesterId, receiverId });

  const respondLink = (linkId, status) =>
    httpRequest.patch(`/api/bookmark-links/${linkId}`, { status });

  const getSentLinks = (requesterId, status) =>
    httpRequest.get("/api/bookmark-links/sent", { requesterId, status });

  const getReceivedLinks = (receiverId, status) =>
    httpRequest.get("/api/bookmark-links/received", { receiverId, status });

  const deleteLink = (requesterId, receiverId) =>
    httpRequest.delete("/api/bookmark-links", {
      params: { requesterId, receiverId },
    });

  const toggleBookmarkVisibility = (userId, restaurantId, isPublic) =>
    httpRequest.patch("/api/bookmark/visibility", { userId, restaurantId, isPublic });

  const togglePromotionAgree = (userId, restaurantId, promotionAgree) =>
    httpRequest.patch("/api/bookmark/promotion", null, {
      params: { userId, restaurantId, promotionAgree },
    });

  const getSharedBookmarks = (requesterId, targetUserId) =>
    httpRequest.get("/api/bookmark/shared", { requesterId, targetUserId });

  const getMyBookmarks = (userId) =>
    httpRequest.get("/api/bookmark/list", { userId });

  return {
    searchUserByEmail,
    searchUsersByEmail,
    requestLink,
    respondLink,
    getSentLinks,
    getReceivedLinks,
    deleteLink,
    toggleBookmarkVisibility,
    togglePromotionAgree,
    getSharedBookmarks,
    getMyBookmarks,
  };
};
