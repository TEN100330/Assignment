package com.example.assignment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment.databinding.ActivityMainBinding
import com.example.assignment.databinding.FragmentPostBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class PostFragment : Fragment() {

    private lateinit var binding: FragmentPostBinding
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

//    private val galleryLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result: ActivityResult ->
//        if (result.resultCode == AppCompatActivity.RESULT_OK) {
//            val uri = result.data?.data
//            try {
//                val inputStream = context?.contentResolver?.openInputStream(uri!!)
//                val myBitmap = BitmapFactory.decodeStream(inputStream)
//                val stream = ByteArrayOutputStream()
//                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//                val bytes = stream.toByteArray()
//                sImage = Base64.encodeToString(bytes, Base64.DEFAULT)
//                binding.postImg.setImageBitmap(myBitmap)
//                inputStream?.close()
//                Toast.makeText(context, "Image Selected", Toast.LENGTH_SHORT).show()
//            } catch (ex: Exception) {
//                Toast.makeText(context, ex.message.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize the Spinner
        val spinner = binding.postTagSpinner

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.spinner_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                binding.postTag.text = selectedItem
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        binding.selectImgButton.setOnClickListener() {
            val myfileintent = Intent(Intent.ACTION_GET_CONTENT)
            myfileintent.type = "image/*"
           // galleryLauncher.launch(myfileintent)
            ActivityResultLauncher.launch(myfileintent)
        }

        binding.createPostButton.setOnClickListener(){
            addPost()
        }

        binding.showlistButton.setOnClickListener() {
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

        binding.savePostButton.setOnClickListener() {
            updateData()
        }

        binding.delPostButton.setOnClickListener(){
            deleteData()
        }
        return root
    }

    private fun deleteData() {
        db = FirebaseDatabase.getInstance().getReference("posts")
        db.child(nodeId).removeValue().addOnSuccessListener {
            binding.postTagSpinner.setSelection(-1)
            binding.titleText.text.clear()
            binding.descriptionText.text.clear()
            sImage = ""
            binding.postImg.setImageBitmap(null)
            binding.savePostButton.visibility=View.INVISIBLE
            binding.delPostButton.visibility=View.INVISIBLE
            binding.createPostButton.visibility=View.VISIBLE

            Toast.makeText(context, "Post Deleted!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener (){
            Toast.makeText(context,it.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateData() {
        val tag = binding.postTag.text.toString()
        val title = binding.titleText.text.toString()
        val description = binding.descriptionText.text.toString()
        val dtClass = DtClass()
        db = FirebaseDatabase.getInstance().getReference("posts")
        val post = postDs(tag, title, description, sImage)
            db.child(nodeId).setValue(post).addOnSuccessListener {
            binding.postTagSpinner.setSelection(-1)
            binding.titleText.text.clear()
            binding.descriptionText.text.clear()
            sImage = ""
            binding.postImg.setImageBitmap(null)
                binding.savePostButton.visibility=View.INVISIBLE
                binding.delPostButton.visibility=View.INVISIBLE
                binding.createPostButton.visibility=View.VISIBLE

            Toast.makeText(context, "Post Updated!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Fail to update post!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayData() {
        db=FirebaseDatabase.getInstance().getReference("posts")
         db.child(nodeId).get().addOnSuccessListener {
            if(it.exists()){
                val dtClass = DtClass()
                binding.postTag.setText(it.child("postTag").value.toString())
                binding.titleText.setText(it.child("postTitle").value.toString())
                binding.descriptionText.setText(it.child("postDescription").value.toString())
                sImage = it.child("postImage").value.toString()
                val bytes = Base64.decode(sImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
                binding.postImg.setImageBitmap(bitmap)
                binding.delPostButton.visibility=View.VISIBLE
                binding.savePostButton.visibility=View.VISIBLE
                binding.createPostButton.visibility=View.INVISIBLE
            }
        }
    }

    private fun addPost() {
        val tag = binding.postTag.text.toString()
        val title = binding.titleText.text.toString()
        val description = binding.descriptionText.text.toString()
        db = FirebaseDatabase.getInstance().getReference("posts")
        val post = postDs(tag, title, description, sImage)
        val databaseReference = FirebaseDatabase.getInstance().reference
        val id = databaseReference.push().key
        db.child(id.toString()).setValue(post).addOnSuccessListener {
            binding.postTagSpinner.setSelection(-1)
            binding.titleText.text.clear()
            binding.descriptionText.text.clear()
            sImage = ""
            binding.postImg.setImageBitmap(null)
            Toast.makeText(context, "Post Created!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, "Fail to create post!", Toast.LENGTH_SHORT).show()
        }
    }
}
