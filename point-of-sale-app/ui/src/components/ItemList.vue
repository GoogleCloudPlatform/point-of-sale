<template>
  <div class="list-group">
    <button
      class="list-group-item item"
      v-for="item in items"
      v-bind:key="item.id"
      v-on:click="add(item)"
    >
      <strong>{{ item.name }}</strong> - {{ currency(item.price) }}
    </button>
  </div>
</template>

<script>
import Utils from "@/services/lib/Util";

export default {
  name: "ItemList",
  props: {
    items: {
      type: Array,
      required: true,
    },
  },
  data: () => {
    return {};
  },
  methods: {
    add(item) {
      this.$emit("add", item);
    },
    currency(number) {
      return Utils.toCurrency(number);
    },
  },
};
</script>

<style scoped></style>
