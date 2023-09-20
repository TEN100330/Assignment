package com.example.assignment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.assignment.databinding.FragmentForumBinding
import com.example.assignment.databinding.FragmentListBinding
import com.example.assignment.databinding.FragmentPostBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream


class ForumFragment : Fragment() {

    private lateinit var binding: FragmentForumBinding
    private lateinit var db: DatabaseReference
    var sImage: String? = ""
    var nodeId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            nodeId = it.getString("post_id").toString()

        }
    }

    private val ActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    )
    { result: ActivityResult ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            Log.d("Ten Jia Le", "Button Clicked")
            val uri = result.data!!.data
            try {
                val inputStream = context?.contentResolver?.openInputStream(uri!!)
                val myBitmap = BitmapFactory.decodeStream(inputStream)
                val stream = ByteArrayOutputStream()
                myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val bytes = stream.toByteArray()
                sImage = Base64.encodeToString(bytes, Base64.DEFAULT)
                binding.postImg.setImageBitmap(myBitmap)
                inputStream!!.close()
                Toast.makeText(context, "Image Selected", Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                Toast.makeText(context, ex.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForumBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.showListButton.setOnClickListener() {
            val fragment = ListFragment()
            val fragmentManager=activity?.supportFragmentManager
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            fragmentTransaction.replace(R.id.frameLayout,fragment)
                .addToBackStack(PostFragment().toString())
                .commit()
        }

        Log.i("So Yeon Kee",nodeId)
        if (nodeId!=""){
            Log.i("Lay",nodeId)
            displayData()
        }

        return root
    }


    private fun displayData() {
        db=FirebaseDatabase.getInstance().getReference("posts")
        db.child(nodeId).get().addOnSuccessListener {
            if(it.exists()){
                val dtClass = DtClass()
                binding.postTitle.text = it.child("postTitle").value.toString()
                binding.postDesc.text = it.child("postDescription").value.toString()
                sImage = it.child("postImage").value.toString()
                val bytes = Base64.decode(sImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
                binding.postImg.setImageBitmap(bitmap)
            }
        }
    }
}