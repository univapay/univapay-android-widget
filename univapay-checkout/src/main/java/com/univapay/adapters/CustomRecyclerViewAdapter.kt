package com.univapay.adapters

import android.support.v7.widget.RecyclerView

abstract class CustomRecyclerViewAdapter<T: RecyclerView.ViewHolder> : RecyclerView.Adapter<T>(), StoredDataAdapter
