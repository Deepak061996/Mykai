package com.mykaimeal.planner.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterCookwareItemBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.CookwareModel

class CookWareAdapter(private var datalist: MutableList<CookwareModel>, private var requireActivity: FragmentActivity): RecyclerView.Adapter<CookWareAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterCookwareItemBinding =
            AdapterCookwareItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data=datalist[position]

        holder.binding.tvTitleName.text =data.name?:""

        data.image_url?.let {
            Glide.with(requireActivity)
                .load(it)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.no_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.layProgess.root.visibility= View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.layProgess.root.visibility= View.GONE
                        return false
                    }
                })
                .into(holder.binding.imageData)
        }?:run {
            holder.binding.layProgess.root.visibility= View.GONE
        }



    }


    override fun getItemCount(): Int {
        return datalist.size
    }


    class ViewHolder(var binding: AdapterCookwareItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}