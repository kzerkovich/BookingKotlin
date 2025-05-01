package domain.services

interface PaymentGateway {
    companion object {
        fun processPayment(data: String, totalPrice: Double): Boolean {
            return true
        }

        fun refundPayment(userId: Int, amount: Double) {
            println("Refund $amount to user $userId")
        }
    }
}