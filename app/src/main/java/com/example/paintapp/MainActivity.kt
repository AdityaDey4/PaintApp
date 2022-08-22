package com.example.paintapp

import android.Manifest
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kyanogen.signatureview.SignatureView
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    lateinit var signature_view: SignatureView
    lateinit var btnEraser: ImageButton
    lateinit var btnSave: ImageButton
    lateinit var btnColor: ImageButton
    lateinit var seekBar: SeekBar
    lateinit var txtPenSize: TextView

    var defaultColor by Delegates.notNull<Int>()
    var allPermissionGranted = false

    lateinit var fileName: String
    val path: File = File(Environment.getExternalStorageDirectory().absolutePath+"/myPaintings") //folderName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        signature_view = findViewById(R.id.signature_view)
        btnEraser = findViewById(R.id.btnEraser)
        btnColor = findViewById(R.id.btnColor)
        btnSave = findViewById(R.id.btnSave)
        seekBar = findViewById(R.id.penSize)
        txtPenSize = findViewById(R.id.txtPenSize)

        askPermission()

        val format = SimpleDateFormat("yyyymmdd_hhmmss", Locale.getDefault())
        val date = format.format(Date())
        fileName = "$path/$date.png"

        if(!path.exists()) {
            path.mkdirs()
        }

        defaultColor = ContextCompat.getColor(this@MainActivity, R.color.black)
        btnColor.setOnClickListener {
            openColorPicker()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtPenSize.text = "$progress %"
                signature_view.penSize = progress.toFloat()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnEraser.setOnClickListener {
            signature_view.clearCanvas()
        }
        btnSave.setOnClickListener {
            if(!allPermissionGranted) {
                Toast.makeText(this, "At first accept the permission", Toast.LENGTH_SHORT).show()
            }else{
                if(!signature_view.isBitmapEmpty){
                    saveImage()
                }
            }
        }
    }

    private fun saveImage() {
        val file = File(fileName)
        val bitmap = signature_view.signatureBitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream)

        val byteArray = byteArrayOutputStream.toByteArray()
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(byteArray)
        fileOutputStream.flush()
        fileOutputStream.close()

        Toast.makeText(this, "Painting Saved", Toast.LENGTH_SHORT).show()
    }

    private fun openColorPicker() {
        val ambilWarnaDialog = AmbilWarnaDialog(this, defaultColor, object : AmbilWarnaDialog.OnAmbilWarnaListener{
            override fun onCancel(dialog: AmbilWarnaDialog?) {

            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                defaultColor = color
                signature_view.penColor = color
            }
        })
        ambilWarnaDialog.show()
    }

    private fun askPermission() {
        Dexter.withContext(this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    allPermissionGranted = true
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()
    }

}