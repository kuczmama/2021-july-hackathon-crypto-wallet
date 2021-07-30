package com.example.bitcoin.ui.receive

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.bitcoin.R
import com.example.bitcoin.WalletAppKitFactory
import com.example.bitcoin.databinding.FragmentReceiveBinding
import com.google.zxing.WriterException
import org.bitcoinj.wallet.Wallet

class ReceiveFragment : Fragment() {
    // This property is only valid between onCreateView and
    // onDestroyView.
    val TAG = ReceiveFragment::class.simpleName
    private var _binding: FragmentReceiveBinding? = null
    private val binding get() = _binding!!

    private var bitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentReceiveBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val qrCodeImage: ImageView = root.findViewById(R.id.qr_code)

        val wallet: Wallet = WalletAppKitFactory.getInstance(root.context).wallet()

        // TODO Make them dynamic size instead of static
        val width: Int = 500
        val height: Int = 500
        val dimension = if (width < height) width else height
        val publicKey  = wallet.freshReceiveAddress().toString()

        val qrgEncoder = QRGEncoder(publicKey, null, QRGContents.Type.TEXT, dimension)
        qrgEncoder.colorBlack = Color.WHITE
        qrgEncoder.colorWhite = Color.BLACK
        try {
            // Getting QR-Code as Bitmap
            bitmap = qrgEncoder.bitmap
            // Setting Bitmap to ImageView
            qrCodeImage.setImageBitmap(bitmap)
        } catch (e: WriterException) {

            Log.v(TAG, e.toString())
        }


        val tvPublicKey: TextView = root.findViewById(R.id.tv_key)
        tvPublicKey.text = publicKey

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}