package com.example.trackie;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder> {

    private Context context;
    private List<Receipt> receiptList;

    public ReceiptAdapter(Context context, List<Receipt> receiptList) {
        this.context = context;
        this.receiptList = receiptList;
    }

    @NonNull
    @Override
    public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.receipt_item, parent, false);
        return new ReceiptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptViewHolder holder, int position) {
        Receipt receipt = receiptList.get(position);
        holder.receiptDate.setText("Date: " + receipt.getDate());
        holder.receiptTotal.setText("Total: " + receipt.getTotal());
        String category = receipt.getCategory();
        if (category == null || category.isEmpty()) {
            category = "Uncategorised";
        }
        holder.receiptCategory.setText("Category: " + receipt.getCategory());

        String url = receipt.getImageUrl();
        Log.d("ReceiptAdapter", " Image URL: " + url);

//loading the image using glide
        Glide.with(context)
                .load(receipt.getImageUrl())
                .into(holder.receiptImage);

    }


    @Override
    public int getItemCount() {
        return receiptList.size();
    }

    public static class ReceiptViewHolder extends RecyclerView.ViewHolder {
        ImageView receiptImage;
        TextView receiptDate, receiptTotal, receiptCategory;

        //loops through data in firebase and displays it in the recycler view
        public ReceiptViewHolder(View itemView) {
            super(itemView);
            receiptImage = itemView.findViewById(R.id.receiptImage);
            receiptDate = itemView.findViewById(R.id.receiptDate);
            receiptTotal = itemView.findViewById(R.id.receiptTotal);
            receiptCategory = itemView.findViewById(R.id.receiptCategory);
        }
    }
}
