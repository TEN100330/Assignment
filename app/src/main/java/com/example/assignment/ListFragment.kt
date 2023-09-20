package com.example.assignment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.assignment.databinding.FragmentListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener



class ListFragment : Fragment() {
    private lateinit var db:DatabaseReference
    private lateinit var postArrayList:ArrayList<postDs>
    private lateinit var nodeList:ArrayList<tempData>
    private var d1:Long =0
    private var d2:Long =0

    private lateinit var binding:FragmentListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater,container,false)
        //binding.postList.layoutManager=LinearLayoutManager(context)
        binding.postList.layoutManager= StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.postList.hasFixedSize()
        postArrayList= arrayListOf<postDs>()
        nodeList= arrayListOf<tempData>()
        getItemData()
        return binding.root
    }

    private fun getItemData() {
       db = FirebaseDatabase.getInstance().getReference("posts")
        var query:Query
        query = db.orderByChild("postTitle")
        query.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var ky:String =""
                    var pots:String=""
                    for(postsnapshot in snapshot.children){
                        val post = postsnapshot.getValue(postDs::class.java)
                        postArrayList.add(post!!)
                        ky = postsnapshot.key.toString()
                        pots=post.postTitle.toString()
                        val tmppost = tempData(ky,pots)
                        nodeList.add(tmppost)
                    }
                    var adapter = postAdapter(postArrayList)
                    binding.postList.adapter = adapter
                    adapter.setOnItemClickListener(object :postAdapter.OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            val ctpost = nodeList[position]
                            val nodePath = ctpost.id.toString()
                            val fragment = ForumFragment()
                            //val fragment = PostFragment()
                            val bundle=Bundle()
                            bundle.putString("post_id",nodePath.toString())
                            fragment.arguments = bundle
                            val fragmentManager=activity?.supportFragmentManager
                            val fragmentTransaction = fragmentManager!!.beginTransaction()
                            fragmentTransaction.replace(com.example.assignment.R.id.frameLayout,fragment)
                                .commit()
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}