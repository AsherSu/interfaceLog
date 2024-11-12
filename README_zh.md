# halo-plugin-interfaceLog

这是一个 Halo 插件，用于记录接口日志。包括请求参数、响应参数、请求时间、请求方法、请求路径、请求 IP、请求耗时、请求状态码、请求来源等信息。


## 特性说明

- 支持记录请求参数、响应参数、请求时间、请求方法、请求路径、请求IP、请求耗时、请求状态码、请求来源等信息。
- 支持接口日志的按条件搜索、分页等操作。
- 支持接口记录自定义规则配置。
- 支持接口记录规则的导入导出。
- 支持接口记录的清理：定时和手动。


## 使用说明

下载插件后在 Halo 后台管理界面启用插件，然后进入插件配置页面，配置接口记录规则。rules_example.json 为示例规则，可以导入到插件中使用。
在没有配置接口记录规则时，默认不记录所有接口请求。
```json
{
  "interfaceLogRules": [
    {
      "isInclude": true,
      "rule": "/apis/**",
      "version": "1"
    },
    {
      "isInclude": false,
      "rule": "/**/*.js",
      "version": "1"
    },
    {
      "isInclude": false,
      "rule": "/**/*.css",
      "version": "1"
    },
    {
      "isInclude": false,
      "rule": "/apis/dailyActive.halo.run/v1alpha1/interfaceLog/*",
      "version": "1"
    }
  ]
}
```