<!--
 Copyright 2021 Google LLC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

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
