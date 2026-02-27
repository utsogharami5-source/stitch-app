package com.smartbudge.app.data.local.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.smartbudge.app.data.local.dao.TransactionDao
import com.smartbudge.app.data.local.entity.TransactionEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var transactionDao: TransactionDao

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val body = sms.messageBody
                val sender = sms.displayOriginatingAddress
                
                // Basic parsing logic for common MFS/Banks in Bangladesh (e.g., bKash, Nagad)
                parseSms(body, sender)?.let { transaction ->
                    scope.launch {
                        transactionDao.insertTransaction(transaction)
                    }
                }
            }
        }
    }

    private fun parseSms(body: String, sender: String): TransactionEntity? {
        // Example bKash Cash Out: "Cash Out: Tk 500.00 to 017... Successful. Fee Tk 9.25. Balance Tk 100.00. TrxID ..."
        // Example bKash Payment: "Payment: Tk 200.00 to Merchant... Successful. Balance Tk 300.00. TrxID ..."
        
        val amountPattern = Pattern.compile("(?i)(?:Tk|BDT)\\s*([0-9,.]+)")
        val matcher = amountPattern.matcher(body)
        
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            val amount = amountStr?.toDoubleOrNull() ?: return null
            
            val isExpense = body.contains("Payment", ignoreCase = true) || 
                            body.contains("Cash Out", ignoreCase = true) ||
                            body.contains("Send Money", ignoreCase = true) ||
                            body.contains("Paid", ignoreCase = true)

            val isIncome = body.contains("Received", ignoreCase = true) ||
                           body.contains("Cash In", ignoreCase = true) ||
                           body.contains("Salary", ignoreCase = true)

            val type = when {
                isExpense -> "Expense"
                isIncome -> "Income"
                else -> return null // Not a clear transaction
            }

            return TransactionEntity(
                user_id = "user_123", // Dummy for MVP
                type = type,
                amount = amount,
                category_id = 0, // Uncategorized
                date = System.currentTimeMillis(),
                note = "Auto-detected from $sender"
            )
        }
        return null
    }
}
