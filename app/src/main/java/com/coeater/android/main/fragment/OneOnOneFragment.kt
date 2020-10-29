package com.coeater.android.main.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.code.CodeActivity
import com.coeater.android.main.MainActivity
import com.coeater.android.main.MainViewModel
import com.coeater.android.main.MainViewModelFactory
import com.coeater.android.main.recyclerview.addFriendAdapter
import com.coeater.android.webrtc.CallActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.fragment_oneonone.*
import kotlinx.android.synthetic.main.view_main_friends.view.*

private val TAG = "OneOnOneFragment"

class OneOnOneFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels {
        MainViewModelFactory(
            provideUserApi(requireContext())
        )
    }

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
        main_button_invite_friend.configure(
            R.drawable.send_24_px,
            "Invite Friend",
            "Invite your friend with code"
        )
        main_button_search_friend.configure(
            R.drawable.search_24_px,
            "Look for Co-eater",
            "Find another Co-eater to eat with you"
        )
        main_button_invite_friend.setOnClickListener {
//            checkPermission()
            (activity as MainActivity).replaceFragment(
                "OneOnOneCode",
                OneOnOneCodeFragment.State.SHARE
            )
        }
        main_button_search_friend.setOnClickListener {
            val intent = Intent(getActivity(), CodeActivity::class.java)
            startActivity(intent)
        }
        setRecyclerView(include_friends.rv_friends)
    }

    private fun setRecyclerView(FriendsRecyclerView: RecyclerView) {
        viewModel.friendsInfo.observe(viewLifecycleOwner, Observer { friendsInfo ->
            FriendsRecyclerView.apply {
                adapter = addFriendAdapter(requireContext(), friendsInfo.friends)
                layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            }
        })
    }

//    private fun openCallActivity() {
//        Log.i(TAG, "Open Call Activity")
//        val txtUrl = EditText(this.context)
//
//        txtUrl.hint = "7자 숫자"
//
//        AlertDialog.Builder(this.context)
//            .setTitle("Test ID")
//            .setMessage("Paste in the link of an image to moustachify!")
//            .setView(txtUrl)
//            .setPositiveButton("확인",
//                DialogInterface.OnClickListener { dialog, whichButton ->
//                    val url = txtUrl.text.toString()
//                    val intent = Intent(getActivity(), CallActivity::class.java)
//                    intent.putExtra("url", url)
//                    startActivity(intent)
//                })
//            .setNegativeButton("Cancel",
//                DialogInterface.OnClickListener { dialog, whichButton -> })
//            .show()
//    }
//
//    private fun checkPermission() {
//        Log.i(TAG, "Check Permission")
//        val permissionListener: PermissionListener = object : PermissionListener {
//            override fun onPermissionGranted() {
//                Toast.makeText(
//                    this@OneOnOneFragment.context,
//                    "Permission Granted",
//                    Toast.LENGTH_SHORT
//                ).show()
//                openCallActivity()
////                (activity as MainActivity).replaceFragment("OneOnOneCode", OneOnOneCodeFragment.State.SHARE)
//            }
//
//            override fun onPermissionDenied(deniedPermissions: List<String>) {
//                Toast.makeText(
//                    this@OneOnOneFragment.context,
//                    "Permission Denied\n$deniedPermissions",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//
//        TedPermission.with(this.context)
//            .setPermissionListener(permissionListener)
//            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
//            .setPermissions(
//                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.CAMERA,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//            .check()
//    }
}
