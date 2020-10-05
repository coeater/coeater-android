package com.coeater.android.main.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.coeater.android.R
import com.coeater.android.webrtc.CallActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.fragment_oneonone.*


class OneOnOneFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_oneonone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    private fun setup() {
        main_button_invite_friend.configure("Invite Friend", "Invite your friend with code")
        main_button_search_friend.configure("Look for Co-eater", "Find another Co-eater to eat with you")
        main_button_invite_friend.setOnClickListener {
            checkPermission()
        }
    }

    private fun openCallActivity() {
        val txtUrl = EditText(this.context)

        txtUrl.hint = "7자 숫자"

        AlertDialog.Builder(this.context)
            .setTitle("Test ID")
            .setMessage("Paste in the link of an image to moustachify!")
            .setView(txtUrl)
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    val url = txtUrl.text.toString()
                    val intent = Intent(getActivity(), CallActivity::class.java);
                    intent.putExtra("url", url);
                    startActivity(intent)
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton -> })
            .show()


    }

    private fun checkPermission() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(this@OneOnOneFragment.context, "Permission Granted", Toast.LENGTH_SHORT).show()
                openCallActivity()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(
                    this@OneOnOneFragment.context,
                    "Permission Denied\n$deniedPermissions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        TedPermission.with(this.context)
            .setPermissionListener(permissionListener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check()
    }
}
