export const MARKER_BASE_PATH =
  "M16 1C8.8 1 3 6.8 3 14c0 9.3 13 30 13 30s13-20.7 13-30C29 6.8 23.2 1 16 1z";
export const MARKER_STAR_PATH =
  "M16 8.5l2.1 4.3 4.7.7-3.4 3.3.8 4.7-4.2-2.2-4.2 2.2.8-4.7-3.4-3.3 4.7-.7z";

export const buildMarkerBody = (fill, accentMarkup) =>
  `<path d="${MARKER_BASE_PATH}" fill="${fill}" stroke="white" stroke-width="2"/>${accentMarkup}`;

export const buildMarkerCircle = (fill = "white") =>
  `<circle cx="16" cy="14" r="5" fill="${fill}"/>`;

export const buildMarkerStar = (fill = "white") =>
  `<path d="${MARKER_STAR_PATH}" fill="${fill}"/>`;

export const buildMarkerDataUri = (
  body,
  { width = 32, height = 46, viewBox = "0 0 32 46" } = {}
) =>
  `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="${viewBox}">${body}</svg>`
  )}`;
