package sample.nfc.reader

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.MifareClassic
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import hnu.nfc.reader.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {
        // Used to load the 'reader' library on application startup.
        init {
            System.loadLibrary("decoder")
        }
    }

    private lateinit var binding: ActivityMainBinding

    private var adapter: NfcAdapter? = null

    private lateinit var pendingIntent: PendingIntent

    private val intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))

    private val techLists = arrayOf(arrayOf("android.nfc.tech.MifareClassic"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = NfcAdapter.getDefaultAdapter(this)

        adapter?.let {
            pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            )
        }
    }

    override fun onResume() {
        super.onResume()
        adapter?.let {
            if (adapter!!.isEnabled) {
                adapter!!.enableForegroundDispatch(this, pendingIntent, intentFilters, techLists)
            } else {
                Toast.makeText(this, "NFC service is unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        adapter?.let {
            adapter!!.disableForegroundDispatch(this)
        }
    }

    private external fun decodeNfc(block: ByteArray): Float

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        try {
            val mifare = MifareClassic.get(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG))
            mifare.connect()

            val sector = 13
            val key = byteArrayOf(-0x7a, 0x52, 0x00, 0x36, 0x19, -0x7a)

            if (mifare.authenticateSectorWithKeyA(sector, key) && mifare.authenticateSectorWithKeyB(sector, key)) {
                val money = decodeNfc(mifare.readBlock(mifare.sectorToBlock(sector)))

                Toast.makeText(this, "%.2f".format(money), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "MifareClassic auth failed", Toast.LENGTH_SHORT).show()
            }
        } catch (err: Throwable) {
            Log.e("TAG", "", err)
            Toast.makeText(this, "NFC 读取错误", Toast.LENGTH_SHORT).show()
        }
    }
}
