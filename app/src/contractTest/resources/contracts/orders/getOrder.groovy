import static org.springframework.cloud.contract.spec.Contract.make

[
	make {
		name "get order by id successfully"
		request {
			method GET()
			url "/api/v1/orders/order-id-1"
			headers {
				header authorization(), 'user-token'
			}
		}
		response {
			status OK()
			headers {
				contentType('application/json')
			}
			body(
					code: 0,
					message: "查询成功",
					data: [
						orderId: $(consumer(anyNonEmptyString()), producer('order-id-1')),
						orderNumber: $(consumer(anyNonEmptyString()), producer('20251105102730996280')),
						userId: $(consumer(anyNonEmptyString()), producer('user-token')),
						merchantId: $(consumer(anyNonEmptyString()), producer('merchant-001')),
						items: [
							[
								dishId: $(consumer(anyNonEmptyString()), producer('dish-001')),
								dishName: $(consumer(anyNonEmptyString()), producer('宫保鸡丁')),
								quantity: $(consumer(anyNumber()), producer(2)),
								price: $(consumer(anyNumber()), producer(25.00))
							]
						],
						deliveryInfo: [
							recipientName: $(consumer(anyNonEmptyString()), producer('张三')),
							recipientPhone: $(consumer(anyNonEmptyString()), producer('13800138000')),
							address: $(consumer(anyNonEmptyString()), producer('北京市朝阳区xxx街道xxx号'))
						],
						remark: $(consumer(anyAlphaUnicode()), producer('少辣')),
						status: $(consumer(anyNonEmptyString()), producer('PENDING_PAYMENT')),
						pricing: [
							itemsTotal: $(consumer(anyNumber()), producer(50.00)),
							packagingFee: $(consumer(anyNumber()), producer(1.00)),
							deliveryFee: $(consumer(anyNumber()), producer(3.00)),
							finalAmount: $(consumer(anyNumber()), producer(54.00))
						],
						createdAt: $(consumer(anyNonEmptyString()), producer('2025-11-05T02:27:30.745152Z'))
					]
					)
		}
	},
	make {
		name "get order not found"
		request {
			method GET()
			url "/api/v1/orders/non-existent-order-id"
			headers {
				header authorization(), 'user-token'
			}
		}
		response {
			status NOT_FOUND()
			headers {
				contentType('application/problem+json')
			}
			body(
					type: "about:blank",
					title: "OrderNotFoundException",
					status: 404,
					detail: $(consumer(anyNonEmptyString()), producer("订单不存在"))
					)
		}
	}
]
