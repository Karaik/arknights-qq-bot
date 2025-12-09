# 鉴权调用流程（Koishi/后端对接）

本文档描述当前后端暴露的鉴权接口，以及如何按官方链路逐步拿到 cred/token。所有配置均可在 `application.yml` 的 `auth.*` 下调整。

## 时序概览（短信登录链路）

```
手机号 -> /api/auth/phone-code           -> 发送短信验证码
     (用户输入验证码)
         -> /api/auth/token-by-code       -> 得到登录 token
         -> /api/auth/grant               -> 换取 oauth_code + uid
         -> /api/auth/cred                -> 换取 cred + skland token
         -> /api/auth/cred/check          -> 带签名校验 cred 有效性
```

## 接口摘要
- `POST /api/auth/phone-code`：body `{phone,type}`，默认 `type=2`。
- `POST /api/auth/token-by-code`：body `{phone,code}`，返回登录 token。
- `POST /api/auth/grant`：body `{token}`，返回 `code`（oauth_code）与 `uid`。
- `POST /api/auth/cred`：body `{code,kind=1}`，返回 `cred` 与用于签名的 `token`。
- `POST /api/auth/cred/check`：body `{cred,token}`，内部自动生成签名头后请求官方 `/user/check`。

## 签名规则（适用于所有需要 `Cred` 的功能接口）
- header 基础字段按顺序：`platform`,`timestamp`,`dId`,`vName`，值来源 `auth.skland.*` 配置。
- 待签名字符串：`path + query(不含?) + bodyJson + timestamp + headersJson`。
- `sign = md5(hmac_sha256(token, 待签名字符串))`，`token` 为 `generate_cred_by_code` 的返回值。
- 请求头需包含：`cred`（接口返回）、`sign`、上述基础字段。

## 配置说明
```
auth:
  hypergryph:
    base-url: 官方 as.hypergryph.com
    send-phone-code: /general/v1/send_phone_code
    token-by-phone-code: /user/auth/v2/token_by_phone_code
    grant: /user/oauth2/v2/grant
    app-code: 4ca99fa6b56cc2ba   # 官方固定
    user-agent: ...              # 可按需覆盖
    timeout-seconds: 10
  skland:
    base-url: https://zonai.skland.com
    cred-by-code: /api/v1/user/auth/generate_cred_by_code
    check-cred: /api/v1/user/check
    v-name: 1.35.0
    platform: "1"
    d-id: ""                      # 可留空或自定义设备 ID
```

Koishi 调用时建议严格按顺序执行，便于定位失败环节；功能性接口（带 Cred）请复用 `auth.skland` 的签名规则。后续如需聚合“一步到位”接口，可在保持上述步骤可观测的前提下再做封装。
