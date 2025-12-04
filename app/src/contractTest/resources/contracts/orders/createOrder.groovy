import static org.springframework.cloud.contract.spec.Contract.make

[
	make {
		name "create a new order"
		request {
			method POST()
			url "/api/v1/orders"
			headers {
				header authorization(), 'user-token'
				contentType('application/json')
			}
			body(
					merchantId: "merchant-001",
					items: [
						[
							dishId: "dish-001",
							dishName: "宫保鸡丁",
							quantity: 2,
							price: 25.00
						]
					],
					deliveryInfo: [
						recipientName: "张三",
						recipientPhone: "13800138000",
						address: "北京市朝阳区xxx街道xxx号"
					],
					remark: null
					)
		}
		response {
			status CREATED()
			headers {
				contentType('application/json')
			}
			body(
					code: 0,
					message: "订单创建成功",
					data: [
						orderId: $(consumer(anyNonEmptyString()), producer('order-id-1')),
						orderNumber: $(consumer(anyNonEmptyString()), producer('20251105102730996280')),
						status: "PENDING_PAYMENT",
						pricing: [
							itemsTotal: 50.00,
							packagingFee: 1.00,
							deliveryFee: 3.00,
							finalAmount: 54.00
						],
						createdAt: $(consumer(anyNonEmptyString()), producer('2025-11-05T02:27:30.745152Z'))
					]
					)
		}
	},
	make {
		name "create order with missing merchant id"
		request {
			method POST()
			url "/api/v1/orders"
			headers {
				header authorization(), 'user-token'
				contentType('application/json')
			}
			body(
					merchantId: null,
					items: [
						[
							dishId: "dish-001",
							dishName: "宫保鸡丁",
							quantity: 2,
							price: 25.00
						]
					],
					deliveryInfo: [
						recipientName: "张三",
						recipientPhone: "13800138000",
						address: "北京市朝阳区xxx街道xxx号"
					]
					)
		}
		response {
			status BAD_REQUEST()
			headers {
				contentType('application/problem+json')
			}
			body(
					type: "about:blank",
					title: "ValidationError",
					status: 400,
					detail: $(consumer(anyNonEmptyString()), producer("merchantId: 商家ID不能为空"))
					)
		}
	},
	make {
		name "create order with invalid phone number"
		request {
			method POST()
			url "/api/v1/orders"
			headers {
				header authorization(), 'user-token'
				contentType('application/json')
			}
			body(
					merchantId: "merchant-001",
					items: [
						[
							dishId: "dish-001",
							dishName: "宫保鸡丁",
							quantity: 2,
							price: 25.00
						]
					],
					deliveryInfo: [
						recipientName: "张三",
						recipientPhone: "12345678901",
						address: "北京市朝阳区xxx街道xxx号"
					]
					)
		}
		response {
			status BAD_REQUEST()
			headers {
				contentType('application/problem+json')
			}
			body(
					type: "about:blank",
					title: "ValidationError",
					status: 400,
					detail: $(consumer(anyNonEmptyString()), producer("deliveryInfo.recipientPhone: 手机号格式不正确"))
					)
		}
	},
	make {
		name "create order with empty items"
		request {
			method POST()
			url "/api/v1/orders"
			headers {
				header authorization(), 'user-token'
				contentType('application/json')
			}
			body(
					merchantId: "merchant-001",
					items: [],
					deliveryInfo: [
						recipientName: "张三",
						recipientPhone: "13800138000",
						address: "北京市朝阳区xxx街道xxx号"
					]
					)
		}
		response {
			status BAD_REQUEST()
			headers {
				contentType('application/problem+json')
			}
			body(
					type: "about:blank",
					title: "ValidationError",
					status: 400,
					detail: $(consumer(anyNonEmptyString()), producer("items: 订单至少包含一个餐品"))
					)
		}
	}
]