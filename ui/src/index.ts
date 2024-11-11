import { definePlugin } from "@halo-dev/console-shared";
import HomeView from "./views/HomeView.vue";
import { markRaw } from "vue";
import { IconList } from "@halo-dev/components";
import {ElTable} from "element-plus";

export default definePlugin({
  components: {
    ElTable
  },
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/interfaceLog",
        name: "interfaceLog",
        component: HomeView,
        meta: {
          permissions: ["plugin:interfaceLog:view"],
          title: "接口日志",
          searchable: true,
          menu: {
            name: "接口日志",
            group: "system",
            icon: markRaw(IconList),
            priority: 0,
          },
        },
      },
    },
  ],
  extensionPoints: {},
});
