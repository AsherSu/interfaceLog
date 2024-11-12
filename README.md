# halo-plugin-interfaceLog

[中文说明](./README_zh.md)

This is a Halo plugin for recording interface logs. It includes request parameters, response parameters, request time, request method, request path, request IP, request duration, request status code, request source, etc.


## Features

- Record request parameters, response parameters, request time, request method, request path, request IP, request duration, request status code, request source, etc.
- Support interface log query, export, delete, clear, etc.
- Support interface log query, sort, page, etc.
- Support interface record custom rule configuration.
- Support interface record rule import and export.
- Support interface record cleanup: scheduled and manual.

## Usage

Download the plugin and enable it in the Halo backend management interface. Then go to the plugin configuration page, configure the interface recording rules. rules_example.json is an example rule, which can be imported into the plugin for use.
If no interface recording rules are configured, the plugin will not record any interface requests by default.
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

