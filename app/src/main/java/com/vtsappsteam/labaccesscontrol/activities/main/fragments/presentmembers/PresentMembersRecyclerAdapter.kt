package com.vtsappsteam.labaccesscontrol.activities.main.fragments.presentmembers

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vtsappsteam.labaccesscontrol.R
import com.vtsappsteam.labaccesscontrol.utils.Constants
import kotlinx.android.synthetic.main.present_in_lab_recycler_item.view.*

class PresentMembersRecyclerAdapter :  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var presentMembersList : ArrayList<MemberData> = ArrayList()

    fun submitMembersList(members : List<MemberData>) {
        presentMembersList.clear()
        presentMembersList.addAll(members)
        notifyDataSetChanged()
    }

    fun getMembersList() : ArrayList<MemberData> {return presentMembersList}

    fun addMember(member: MemberData)  {
        var memberAlreadyExists : Boolean = false
        presentMembersList.forEach {
            if(it.id == member.id) {
                memberAlreadyExists = true
                return@forEach
            }
        }
        if(memberAlreadyExists.not()) {
            if(member.rank == Constants.COORDINATOR) {
                presentMembersList.add(0, member)
                notifyItemInserted(0)
            } else {
                presentMembersList.add(member)
                notifyItemInserted(presentMembersList.size - 1)
            }
        }
    }

    fun removeMember(memberId : String) {
        for ((position, listItem) in presentMembersList.withIndex()) {
            if(listItem.id == memberId) {
                presentMembersList.removeAt(position)
                notifyItemRemoved(position)
                break
            }
        }
    }

    class PresentMembersViewHolder(private val membersView: View) : RecyclerView.ViewHolder(membersView) {
        private val name : TextView = membersView.tvFNameLName
        private val rank : TextView = membersView.tvRank
        //private val avatar : ImageView = membersView.imgMemberItemHeader

        @SuppressLint("SetTextI18n")
        fun bind(member : MemberData) {
            name.text = "${member.name} ${member.lastName}"
            rank.text = Constants.resourceStringIdsForRanks[member.rank].let {
                if(it == null) { "Unknown" }
                else membersView.context.getString(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresentMembersViewHolder {
        return PresentMembersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.present_in_lab_recycler_item, parent, false))
    }

    override fun getItemCount(): Int {
        return presentMembersList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PresentMembersViewHolder -> {
                holder.bind(presentMembersList[position])
            }
        }
    }
}

data class MemberData(val id: String, val name: String, val lastName : String, val rank : String)