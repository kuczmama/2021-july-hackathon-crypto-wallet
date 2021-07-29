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
import com.example.bitcoin.databinding.FragmentReceiveBinding
import com.google.zxing.WriterException

class ReceiveFragment : Fragment() {
    // This property is only valid between onCreateView and
    // onDestroyView.
    val TAG = "ReceiveFragment"
    private lateinit var receiveViewModel: ReceiveViewModel
    private var _binding: FragmentReceiveBinding? = null
    private val binding get() = _binding!!

    private var bitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        receiveViewModel =
            ViewModelProvider(this).get(ReceiveViewModel::class.java)

        _binding = FragmentReceiveBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val qrCodeImage: ImageView = root.findViewById(R.id.qr_code)

        // TODO Make them dynamic size instead of static
        val width: Int = 500
        val height: Int = 500
        var dimension = if (width < height) width else height
        var publicKey  = "1E99423A4ED27608A15A2616A2B0E9E52CED330AC530EDCC32C8FFC6A526AEDD"

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


        var tvPublicKey: TextView = root.findViewById(R.id.tv_key)
        tvPublicKey.text = publicKey

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}