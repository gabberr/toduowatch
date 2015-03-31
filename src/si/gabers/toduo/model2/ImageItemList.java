package si.gabers.toduo.model2;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.annotations.Expose;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

public class ImageItemList implements ItemListInterface {
	
	@Expose
	String name;
	
	@Expose
	boolean ticked;
	
	@Expose(serialize = false, deserialize = false)
	static String imageFetchUrl = "http://imagefetch.herokuapp.com/?s=";
	
	@Expose
	String encoded;
	
	@Expose(serialize = false, deserialize = false)
	protected Bitmap img;
	
	@Expose(serialize = false, deserialize = false)
	ItemArrayAdapter adapter;
	
	public ImageItemList(String _name, Bitmap _img) {
		name = _name;
		setImage(_img);
	}
	public ImageItemList(String _name, String url) {
		name = _name;
		new ImageThumbDownloader().execute(url);
//		img = _img;	
	}
	
	public ImageItemList(String _name) {
//		adapter = _adapter;
		name = _name;
		new ImageThumbDownloader().execute(imageFetchUrl + _name);
	}
	
	public void setAdapter(ItemArrayAdapter _adapter) {
		adapter = _adapter;
	}
	
	public ImageItemList(String _name, ItemArrayAdapter _adapter) {
		adapter = _adapter;
		name = _name;
		new ImageThumbDownloader().execute(imageFetchUrl + _name);
	}
	@Override
	public String getItemName() {
		return name;
	}
	
	public void setImage(Bitmap _img) {
		img = _img;
		encodeBitmap();
	}

	@Override
	public boolean isImageItem() {
		return true;
	}
	
	public Bitmap getImage() {
		return img;
	}
	@Override
	public boolean isTicked() {
		return ticked;
	}
	public void setTicked(boolean value) {
		ticked = value;
	}
	
	public void encodeBitmap() {
		if(img != null) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  
	        img.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
	        byte[] byteArray = byteArrayOutputStream .toByteArray();
	        encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
		}
	}
	
	  @Override
	public String toString() {
		return getItemName();
	}
	public void decodeBitmap() {
		if(img == null) {
			
			byte[] imageAsBytes = Base64.decode(encoded.getBytes(),Base64.DEFAULT);	    
			img = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
		    
		}
	}




	private class ImageThumbDownloader extends AsyncTask<String, Void, Bitmap>  {
		  
	        @Override
	        protected Bitmap doInBackground(String... param) {
	            // TODO Auto-generated method stub
	            return downloadBitmap(param[0]);
	        }
	 
	        @Override
	        protected void onPreExecute() {
	            Log.i("Async-Example", "onPreExecute Called");
//	            simpleWaitDialog = ProgressDialog.show(ImageDownladerActivity.this,
//	                    "Wait", "Downloading Image");
	 
	        }
	 
	        @Override
	        protected void onPostExecute(Bitmap result) {
	            Log.i("Async-Example", "onPostExecute Called");
//	            img = result;
	            setImage(result);
	            if(adapter !=null)
	            	adapter.notifyDataSetChanged();
	 
	        }
	 
	        private Bitmap downloadBitmap(String url) {
	            // initilize the default HTTP client object
	            final DefaultHttpClient client = new DefaultHttpClient();
	 
	            //forming a HttoGet request 
	            final HttpGet getRequest = new HttpGet(url);
	            try {
	 
	                HttpResponse response = client.execute(getRequest);
	 
	                //check 200 OK for success
	                final int statusCode = response.getStatusLine().getStatusCode();
	 
	                if (statusCode != HttpStatus.SC_OK) {
	                    Log.w("ImageDownloader", "Error " + statusCode + 
	                            " while retrieving bitmap from " + url);
	                    return null;
	 
	                }
	 
	                final HttpEntity entity = response.getEntity();
	                if (entity != null) {
	                    InputStream inputStream = null;
	                    try {
	                        // getting contents from the stream 
	                        inputStream = entity.getContent();
	 
	                        // decoding stream data back into image Bitmap that android understands
	                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
	 
	                        return bitmap;
	                    } finally {
	                        if (inputStream != null) {
	                            inputStream.close();
	                        }
	                        entity.consumeContent();
	                    }
	                }
	            } catch (Exception e) {
	                // You Could provide a more explicit error message for IOException
	                getRequest.abort();
	                Log.e("ImageDownloader", "Something went wrong while" +
	                        " retrieving bitmap from " + url + e.toString());
	            } 
	 
	            return null;
	        }
}
}