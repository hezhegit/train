<template>
  <a-layout id="components-layout-demo-top-side-2">
    <the-header/>
    <a-layout>
      <the-sider/>
      <a-layout-content
          :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
      >
        <router-view></router-view>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
<script>
import {defineComponent} from 'vue';
import TheHeader from "@/components/the-header.vue";
import TheSider from "@/components/the-sider.vue";
export default defineComponent({
  components: {
    TheSider,
    TheHeader,
  },
  setup() {

    return {
    };
  },
});
</script>
<style>

</style>