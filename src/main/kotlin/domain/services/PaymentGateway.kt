package domain.services


interface PaymentGateway {
    companion object {
        fun processPayment(data: String, totalPrice: Double): Boolean {
            println("Processing payment: $${totalPrice} [Data: ${data}]")
            return true
        }

        fun refundPayment(userId: Int, amount: Double) {
            println("Processing refund: $${amount} to user $userId")
            println("Refund $amount to user $userId")
        }
    }
}