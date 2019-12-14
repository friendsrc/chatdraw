package com.example.chatdraw.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chatdraw.items.TransactionItem;
import com.example.chatdraw.R;

import java.util.LinkedList;

public class CreditRecyclerViewAdapter extends RecyclerView.Adapter<CreditRecyclerViewAdapter.ExampleViewHolder> implements Filterable {
    private LinkedList<TransactionItem> exampleList;
    private LinkedList<TransactionItem> exampleListFull;

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView1;
        public TextView mTextView2;

        public ExampleViewHolder(View itemView) {
            super(itemView);
            mTextView1 = itemView.findViewById(R.id.textView);
            mTextView2 = itemView.findViewById(R.id.textView2);
        }
    }

    // get data from this constructor
    public CreditRecyclerViewAdapter(LinkedList<TransactionItem> exampleList) {
        this.exampleList = exampleList;
        exampleListFull = new LinkedList<>(exampleList);
    }

    // what we want to assign to the view of evry single information
    @Override
    public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new ExampleViewHolder(v);
    }

    // pass information/value here -> position where you are looking at
    @Override
    public void onBindViewHolder(ExampleViewHolder holder, int position) {
        TransactionItem currentItem = exampleList.get(position);

        holder.mTextView1.setText(currentItem.getText1());
        holder.mTextView2.setText(currentItem.getText2());
    }

    @Override
    public int getItemCount() {
        return exampleList.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }


    public void clearData() {
//        exampleList = new LinkedList<>();
//        notifyDataSetChanged();
    }

    public void addData(TransactionItem transactionItem) {
//        exampleList.addFirst(transactionItem);
//        notifyDataSetChanged();
    }


    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            LinkedList<TransactionItem> filteredList = new LinkedList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(exampleListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (TransactionItem item : exampleListFull) {
                    if (item.getText1().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            exampleList.clear();
            exampleList.addAll((LinkedList) results.values);
            notifyDataSetChanged();
        }
    };
}
