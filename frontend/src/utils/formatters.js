export const formatRouteDistance = (meters) => {
  if (!Number.isFinite(meters)) return "-";
  if (meters < 1000) return `${Math.round(meters)}m`;
  return `${(meters / 1000).toFixed(1)}km`;
};

export const formatRouteDurationMinutes = (seconds) => {
  if (!Number.isFinite(seconds)) return "-";
  const totalMinutes = Math.max(1, Math.round(seconds / 60));
  return `${totalMinutes}분`;
};

export const formatRouteDurationDetailed = (seconds) => {
  if (!Number.isFinite(seconds)) return "-";
  const totalMinutes = Math.round(seconds / 60);
  if (totalMinutes < 60) return `${totalMinutes}분`;
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  return `${hours}시간 ${minutes}분`;
};
