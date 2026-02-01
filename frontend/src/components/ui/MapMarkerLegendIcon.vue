<script setup>
import { MARKER_BASE_PATH, MARKER_STAR_PATH } from "@/utils/mapMarkerSvgs";

const props = defineProps({
  fill: {
    type: String,
    default: "#007bff",
  },
  starFill: {
    type: String,
    default: "white",
  },
});
</script>

<template>
  <svg
    xmlns="http://www.w3.org/2000/svg"
    width="16"
    height="20"
    viewBox="0 0 32 46"
    aria-hidden="true"
  >
    <path
      :d="MARKER_BASE_PATH"
      :fill="props.fill"
      stroke="white"
      stroke-width="2"
    />
    <path :d="MARKER_STAR_PATH" :fill="props.starFill" />
  </svg>
</template>
