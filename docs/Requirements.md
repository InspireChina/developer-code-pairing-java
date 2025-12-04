# 需求文档

## 背景

本文档定义"要吃饱"订餐系统中用户下订单功能的需求规格。该功能允许已登录用户选择商家餐品、填写配送信息并创建订单，是整个订餐流程的核心环节。

## 术语表

- **系统**: 要吃饱订餐平台系统
- **用户**: 已注册并登录的平台用户
- **订单**: 用户提交的餐品购买订单
- **商家**: 提供餐品的商家店铺
- **餐品**: 商家提供的菜品

## 需求列表

---

## YCB-001: 实现基本的创建订单功能

**Story描述：** 

实现基本的创建订单功能。用户提交订单时需要提供以下信息：
- 商户ID
- 选择的餐品信息（餐品ID、餐品名称、数量、单价）
- 配送信息（收货人姓名、手机号、收货地址）
- 备注信息（可选，最多200字符）

系统接收订单后需要：
- 生成唯一的订单ID和订单号（格式：yyyyMMddHHmmss + 6位随机数）
- 设置订单状态为"待支付"（PENDING\_PAYMENT）
- 计算订单最终价格

价格计算规则：
- 餐品总价 = 所有餐品的（单价 × 数量）之和
- 打包费 = 1元（固定）
- 配送费 = 3元（固定）
- 订单最终金额 = 餐品总价 + 打包费 + 配送费

### 需求 1.1: 用户创建基本订单

**用户故事：** 作为一个已登录用户，我想要选择商家餐品并提交订单，以便购买我喜欢的餐品。

#### 验收标准

1. 当用户提交包含商户ID、餐品列表、配送信息和备注的订单时，系统应创建一个具有唯一订单ID的新订单
2. 当订单创建时，系统应生成格式为"yyyyMMddHHmmss + 6位随机数"的订单号
3. 当订单创建时，系统应将订单状态设置为"PENDING\_PAYMENT"
4. 当用户提交订单时缺少必填字段，系统应拒绝订单并返回验证错误信息
5. 当用户选择的餐品来自多个商家时，系统应拒绝订单并要求一个订单只能包含一个商家的餐品

### 需求 1.2: 基本价格计算

**用户故事：** 作为用户，我想要看到订单的总价格，以便了解需要支付的金额。

#### 验收标准

1. 当订单创建时，系统应计算餐品总价为所有餐品的单价乘以数量之和
2. 当订单创建时，系统应在订单总额中添加打包费（默认为1元）
3. 当订单创建时，系统应在订单总额中添加配送费（默认为3元）
4. 当计算最终金额时，系统应按照"餐品总价 + 打包费 + 配送费"的公式计算
5. 当订单数据存储时，系统应确保所有金额字段保留2位小数精度

### 需求 1.3: 订单数据完整性

**用户故事：** 作为系统管理员，我需要确保订单数据完整准确，以便进行订单追踪和问题排查。

#### 验收标准

1. 当订单创建时，系统应记录用户ID、商户ID、餐品列表（包含数量和价格）
2. 当订单创建时，系统应记录配送收货人姓名、手机号和收货地址
3. 当订单创建时，系统应记录可选的备注文本，最多200字符
4. 当订单创建时，系统应记录订单创建时间戳
5. 当订单创建时，系统应存储打包费为1.00元，配送费为3.00元

---

## API接口定义

### 创建订单接口

**Endpoint**: `POST /api/v1/orders`

**Headers**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "merchantId": "string (required)",
  "items": [
    {
      "dishId": "string (required)",
      "dishName": "string (required)",
      "quantity": "integer (required, min: 1)",
      "price": "decimal (required)"
    }
  ],
  "deliveryInfo": {
    "recipientName": "string (required)",
    "recipientPhone": "string (required, pattern: ^1[3-9]\\d{9}$)",
    "address": "string (required, max: 500)"
  },
  "remark": "string (optional, max: 200)"
}
```

**字段说明**:
- `merchantId`: 商家ID，必填
- `items`: 订单项列表，至少包含一项
  - `dishId`: 菜品ID
  - `dishName`: 菜品名称
  - `quantity`: 购买数量，必须大于0
  - `price`: 菜品单价
- `deliveryInfo`: 配送信息
  - `recipientName`: 收货人姓名
  - `recipientPhone`: 收货人手机号，11位
  - `address`: 收货地址，详细地址字符串
- `remark`: 订单备注，可选，最多200字符

**Response - Success (201 Created)**:

**Response Body**:
```json
{
  "code": 0,
  "message": "订单创建成功",
  "data": {
    "orderId": "string",
    "orderNumber": "string",
    "status": "PENDING_PAYMENT",
    "pricing": {
      "itemsTotal": 50.00,
      "packagingFee": 1.00,
      "deliveryFee": 3.00,
      "finalAmount": 54.00
    },
    "paymentDeadline": "string (ISO 8601)",
    "createdAt": "string (ISO 8601)"
  }
}
```
