<template>
  <div class="list-group">
    <button
      class="list-group-item item"
      v-for="item in items"
      v-bind:key="item.id"
      v-on:click="add(item)"
    >
      <strong>{{ item.name }}</strong> - {{ item.price }}
    </button>
  </div>
</template>

<script>
export default {
  name: "ItemList",
  components: {},
  props: {
    items: {
      type: Array,
      required: true,
    },
  },
  data: () => {
    return {};
  },
  computed: {},
  created() {},
  methods: {
    add(item) {
      this.$emit("add", item);
    },
  },
};
</script>

<style scoped></style>
