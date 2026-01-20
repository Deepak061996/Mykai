package com.mykaimeal.planner.fragment.mainfragment.profilesetting.orderhistoryscreen.model

import com.stripe.android.model.Card
import java.io.Serializable

data class OrderHistoryModel(
    val code: Int,
    val `data`: MutableList<OrderHistoryModelData>?,
    val message: String,
    val history_status: Int?=0,
    val success: Boolean
)

data class OrderHistoryModelData(
    val address: String?,
    val status: Int?,
    val date: String?,
    val order: Order?,
    val store_logo: String?,
    val card:CardModel?
) : Serializable

data class Order(
    val final_quote: FinalQuote?,
    val is_sandbox: Boolean?,
    val order_id: String?,
    val order_placed: Boolean?,
    val tracking_link: String?
)

data class CardModel(
    val name: String?,
    val card_no: String?,
    val card_type: String?,
    val card_brand: String?
)


data class FinalQuote(
    val added_fees: AddedFees?,
    val items: MutableList<Item>?,
    val misc_fees: MutableList<Any>,
    val quote: Quote?,
    val quote_id: String?,
    val store: String?,
    val store_address: String?,
    val store_id: String?,
    val tip: Double,
    val total_with_tip: Double
)

data class AddedFees(
    val flat_fee_cents: Double,
    val is_fee_taxable: Boolean,
    val percent_fee: Double,
    val sales_tax_cents: Double,
    val total_fee_cents: Double
)

data class Item(
    val base_price: Double?,
    val customizations: MutableList<Any>,
    val image: String?,
    val name: String?,
    val notes: String?,
    val product_id: String?,
    val quantity: Int?
)

data class Quote(
    val delivery_fee_cents: Double?,
    val delivery_time_max: Double?,
    val delivery_time_min: Double?,
    val expected_time_of_arrival: String?,
    val sales_tax_cents: Double,
   /* val scheduled: MutableList<Any>,*/
    val service_fee_cents: Double?,
    val small_order_fee_cents: Double?,
    val subtotal: Double?,
    val total_without_tips: Double?
)






