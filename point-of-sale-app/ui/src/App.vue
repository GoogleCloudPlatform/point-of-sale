<template>
  <div id="app">
    <Main ref="main" />
  </div>
</template>

<script>
import Main from "./components/Main.vue";

export default {
  name: "App",
  components: {
    Main,
  },
  data: () => {
    return {};
  },
};
</script>

<style>

</style>
