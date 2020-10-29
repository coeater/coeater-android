package com.coeater.android.main.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.coeater.android.R
import com.coeater.android.main.MainActivity
import com.coeater.android.webrtc.CallActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.fragment_oneonone_code.*

private val TAG = "CodeFragment"
class OneOnOneCodeFragment(val state: State) : Fragment() {

    enum class State { SHARE, JOIN, ADD }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "On Create View")
        return inflater.inflate(R.layout.fragment_oneonone_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        Log.i(TAG, "On View Created")
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    fun setup() {
        Log.i(TAG, "Setup")
        when (state) {
            State.SHARE -> {
                tv_code_title.text = "Your Code"
                tv_code_number.text = "00000"
                iv_state.setImageResource(R.drawable.share_24_px)
                tv_state.text = "Share"
                et_code_number.visibility = View.GONE
                btn_share_or_join.setOnClickListener {
                    (activity as MainActivity).replaceFragment("OneOnOneCode", State.JOIN)
                }
            }
            State.JOIN -> {
                setEnterCode()
                tv_state.text = "Join"
                et_code_number.visibility = View.VISIBLE
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                btn_share_or_join.setOnClickListener {

                }

                val codeNumber = et_code_number.text.toString()
                val intent = Intent(getActivity(), CallActivity::class.java)
                intent.putExtra("url", codeNumber)
                startActivity(intent)

            }
            State.ADD -> {
                setEnterCode()
                tv_state.text = "Add"
                setAddListener()
            }
        }
    }

    private fun openCallActivity() {
        Log.i(TAG, "Open Call Activity")
        val txtUrl = EditText(this.context)

        txtUrl.hint = "7자 숫자"

        AlertDialog.Builder(this.context)
            .setTitle("Test ID")
            .setMessage("Paste in the link of an image to moustachify!")
            .setView(txtUrl)
            .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, whichButton ->
                    val url = txtUrl.text.toString()
                    val intent = Intent(getActivity(), CallActivity::class.java)
                    intent.putExtra("url", url)
                    startActivity(intent)
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, whichButton -> })
            .show()
    }

    private fun checkPermission() {
        Log.i(TAG, "Check Permission")
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(
                    this@OneOnOneCodeFragment.context,
                    "Permission Granted",
                    Toast.LENGTH_SHORT
                ).show()
                openCallActivity()
//                (activity as MainActivity).replaceFragment("OneOnOneCode", OneOnOneCodeFragment.State.SHARE)
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(
                    this@OneOnOneCodeFragment.context,
                    "Permission Denied\n$deniedPermissions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        TedPermission.with(this.context)
            .setPermissionListener(permissionListener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .check()
    }

    private fun setEnterCode() {
        tv_code_title.text = "Enter Code"
        tv_code_number.text = "_______"
        iv_state.setImageResource(R.drawable.login_24_px)
        et_code_number.visibility = View.VISIBLE
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }


    private fun setAddListener() {
        et_code_number.setOnEditorActionListener { v, actionId, event ->
            if(event.keyCode == KeyEvent.KEYCODE_ENTER) {
                addFriend(et_code_number.text.toString())
                true
            }
            else false
        }

        btn_share_or_join.setOnClickListener { addFriend(et_code_number.text.toString()) }
    }

    private fun addFriend(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }
}
