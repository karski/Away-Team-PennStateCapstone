package edu.psu.team3.app.awayteam;

import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ExpenseEditDialog extends DialogFragment {
	private EditExpenseTask mEditTask = null;

	Uri receiptURI = null;
	Uri newReceiptURI = null;

	private final int IMAGE_FROM_FILE = 2;
	private final int IMAGE_FROM_CAMERA = 1;

	private Date date;
	private double amount = 0;
	private int category = 0;
	private String description = null;

	Button dateView;
	Spinner catSpinner;
	EditText amountView;
	EditText descView;

	Button addReceipt;
	ImageView receiptPreView;
	TextView receiptPathView;

	TeamExpense expense = null;

	// requires "expenseID" for expense to be edited
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// get the passed expenseID
		Bundle args = getArguments();
		expense = UserSession.getInstance(getActivity()).activeTeam
				.getExpense(args.getInt("expenseID"));
		// inflate custom view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle("Edit Expense");
		builder.setIcon(getResources().getDrawable(R.drawable.ic_action_edit));
		builder.setView(inflater.inflate(R.layout.dialog_expense_edit, null))
				// Add action buttons
				.setPositiveButton("Apply",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								ExpenseEditDialog.this.getDialog().cancel();
							}
						});

		return builder.create();

	}

	@Override
	public void onStart() {
		super.onStart(); // super.onStart() is where dialog.show() is actually
							// called on the underlying dialog, so we have to do
							// it after this point
		AlertDialog d = (AlertDialog) getDialog();
		if (d != null) {
			// resize to prevent keyboard from covering dialog buttons
			d.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

			// override create button action to prevent closing immediately
			Button positiveButton = (Button) d
					.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					attemptEditExpense();
				}
			});
			// init UI elements
			date = expense.date;
			dateView = (Button) d.findViewById(R.id.expenseEditDate);
			dateView.setText(DateFormat.getDateInstance(DateFormat.MEDIUM)
					.format(date));
			catSpinner = (Spinner) d.findViewById(R.id.expenseEditCategory);
			catSpinner.setSelection(expense.category.getValue() - 1);
			amountView = (EditText) d.findViewById(R.id.expenseEditAmount);
			String formattedAmount = String.format("%1$,.2f", expense.amount);
			amountView.setText(formattedAmount);
			descView = (EditText) d.findViewById(R.id.expenseEditDescription);
			descView.setText(expense.description);
			// implement picker
			dateView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					DatePickerDialog dateDialog = new DatePickerDialog(
							getActivity(),
							new DatePickerDialog.OnDateSetListener() {

								@Override
								public void onDateSet(DatePicker view,
										int year, int monthOfYear,
										int dayOfMonth) {
									Calendar cal = Calendar.getInstance();
									cal.set(year, monthOfYear, dayOfMonth);
									date = cal.getTime();
									dateView.setText(DateFormat
											.getDateInstance(DateFormat.MEDIUM)
											.format(date));
								}
							}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
							cal.get(Calendar.DAY_OF_MONTH));
					dateDialog.show();
				}
			});
			// init receipt UI
			addReceipt = (Button) d.findViewById(R.id.expenseAddReceipt);
			receiptPreView = (ImageView) d
					.findViewById(R.id.expenseReceiptThumb);
			receiptPathView = (TextView) d
					.findViewById(R.id.expenseReceiptPath);
			if (expense.hasReceipt) {
				addReceipt.setText("Replace Receipt");
			}
			addReceipt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// give user a choice between camera or file
					List<Object[]> choices = new ArrayList<Object[]>();
					choices.add(new Object[] { "Take Picture",
							R.drawable.ic_action_camera });
					choices.add(new Object[] { "Select From File",
							R.drawable.ic_action_collection });
					ListAdapter adapter = new InputListAdapter(getActivity(),
							R.layout.input_entry, choices);
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					builder.setTitle("Choose Image Source");
					builder.setIcon(getActivity().getResources().getDrawable(
							R.drawable.ic_action_new_picture));
					builder.setNegativeButton("cancel",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});

					builder.setAdapter(adapter,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:// camera
											// Try to take a pic
										Intent takePictureIntent = new Intent(
												MediaStore.ACTION_IMAGE_CAPTURE);
										try {
											ContentValues values = new ContentValues();
											values.put(
													MediaStore.Images.Media.TITLE,
													"Receipt.jpg");
											newReceiptURI = getActivity()
													.getContentResolver()
													.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
															values);

											Log.v("CAMERA",
													"TempImage Uri saved as = "
															+ newReceiptURI
																	.toString());
										} catch (Exception e) {
											Log.e("CAMERA",
													"Error creating image file: "
															+ e.toString());
											e.printStackTrace();
										}
										takePictureIntent.putExtra(
												MediaStore.EXTRA_OUTPUT,
												newReceiptURI);

										// Continue only if camera is available
										if (takePictureIntent
												.resolveActivity(getActivity()
														.getPackageManager()) != null) {
											startActivityForResult(
													takePictureIntent,
													IMAGE_FROM_CAMERA);
										}
										break;
									case 1: // file
										// try to get a picture from gallery:
										Intent photoPickerIntent = new Intent(
												Intent.ACTION_PICK);
										photoPickerIntent.setType("image/*");
										startActivityForResult(
												photoPickerIntent,
												IMAGE_FROM_FILE);
										break;
									}

								}
							});
					builder.create().show();
				}
			});
		}
	}

	// listen for the dialog to be closed to reinit actionbar as required
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		try {
			((DisplayActivity) getActivity()).initActionBar();
		} catch (Exception e) {
			Log.e("EXPENSE",
					"Error trying to re-init actionbar: " + e.toString());
			e.printStackTrace();
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v("RESULT", "Result Code: " + resultCode);
		try {
			if (resultCode == Activity.RESULT_OK) {
				if (requestCode == IMAGE_FROM_CAMERA && newReceiptURI == null) {
					// collect the image URI from the activity
					Log.v("RESULT", "stored newReceiptURI was null");
					receiptURI = data.getData();
				} else if (requestCode == IMAGE_FROM_CAMERA) {
					receiptURI = newReceiptURI;
				}
				if (requestCode == IMAGE_FROM_FILE) {
					receiptURI = data.getData();
				}
				Log.v("RESULT",
						"ReceiptURI="
								+ ((receiptURI == null) ? "null" : receiptURI
										.toString()));

				Bitmap imageBitmap = null;
				try {
					Bundle extras = data.getExtras();
					// try to display a thumbnail if it is provided
					imageBitmap = (Bitmap) extras.get("data");
					receiptPreView.setImageBitmap(imageBitmap);
					receiptPreView.setVisibility(View.VISIBLE);
					receiptPathView.setVisibility(View.GONE);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				try {
					//if the camera didn't deliver the bitmap, try getting from file
					if (imageBitmap == null && receiptURI != null) {
						InputStream inStream = getActivity()
								.getContentResolver().openInputStream(
										receiptURI);
						 BitmapFactory.Options bmOptions = new
						 BitmapFactory.Options();
						 bmOptions.inSampleSize = 8;
						imageBitmap = BitmapFactory.decodeStream(inStream,null,bmOptions);
						receiptPreView.setImageBitmap(imageBitmap);
						receiptPreView.setVisibility(View.VISIBLE);
						receiptPathView.setVisibility(View.GONE);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				try {
					// showing the picture didn't work, let's settle for the
					// file name
					if (imageBitmap == null
							&& android.os.Build.VERSION.SDK_INT >= 16) {
						// This will only work with newer SDKs (>=16)
						Cursor cursor = getActivity()
								.getContentResolver()
								.query(receiptURI, null, null, null, null, null);
						if (cursor != null && cursor.moveToFirst()) {
							String displayName = cursor
									.getString(cursor
											.getColumnIndex(OpenableColumns.DISPLAY_NAME));
							Log.i("IMAGE", "Display Name: " + displayName);
							receiptPathView.setText(displayName);
							receiptPreView.setVisibility(View.GONE);
							receiptPathView.setVisibility(View.VISIBLE);
						}
					} else if (imageBitmap == null) {
						receiptPathView.setText("Receipt.jpg");
						receiptPreView.setVisibility(View.GONE);
						receiptPathView.setVisibility(View.VISIBLE);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				addReceipt.setText("Replace Receipt");

				Log.v("RESULT",
						"ReceiptURI="
								+ ((receiptURI == null) ? "null" : receiptURI
										.toString()));
			}
		} catch (Exception e) {
			Log.e("GetPic", "Unable to get image: " + e.toString());
			Log.v("RESULT", "ReceiptURI="
					+ ((receiptURI == null) ? "null" : receiptURI.toString()));
			e.printStackTrace();
			Toast.makeText(getActivity(), "Unable to get Image",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void attemptEditExpense() {
		boolean cancel = false;
		amount = Double.parseDouble(amountView.getText().toString());
		description = descView.getText().toString();
		category = catSpinner.getSelectedItemPosition() + 1;
		if (description.isEmpty()) {
			description = "No Description";
		}
		if (amount <= 0) {
			cancel = true;
			amountView.setError("Check valid amount. Must be more than 0");
			amountView.requestFocus();
		}

		Date now = new Date();
		if (date.compareTo(now) > 0) {
			cancel = true;
			Toast.makeText(getActivity(),
					"Expense date cannot be in the future", Toast.LENGTH_SHORT)
					.show();
		}

		if (!cancel && mEditTask == null) {
			Log.v("Expense", "Starting ExpenseTask. ReceiptURI = "
					+ ((receiptURI == null) ? "null" : receiptURI.toString()));
			mEditTask = new EditExpenseTask();
			mEditTask.execute(receiptURI);
		}

	}

	// edit an expense - receipt is optional parameter
	public class EditExpenseTask extends AsyncTask<Object, Void, Integer> {
		Uri receiptPath = null;
		int expenseID = 0;

		@Override
		protected Integer doInBackground(Object... params) {
			Integer result = 0;
			expenseID = expense.id;
			try {
				if (params.length > 0) {
					receiptPath = (Uri) params[0];
				}
				UserSession s = UserSession.getInstance(getActivity());

				result = CommUtil.EditExpense(getActivity(), s.getUsername(),
						s.currentTeamID, expenseID, date, amount, category,
						description);

				Log.v("Background", "returned from commutil.  result = "
						+ result);
				Log.v("Background",
						"ReceiptPath = "
								+ ((receiptPath == null) ? "null" : receiptPath
										.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mEditTask = null;
			if (result == 1) {// success!
				// try to upload image
				try {
					Log.v("Expense",
							"Entering onPostExecute. ReceiptPath = "
									+ ((receiptPath == null) ? "null"
											: receiptPath.toString()));
					if (receiptPath != null
							&& ((DisplayActivity) getActivity()).mReceiptTask == null) {
						Log.v("Expense",
								"Launching ReceiptUploadTask("
										+ expenseID
										+ ","
										+ ((receiptPath == null) ? "null"
												: receiptPath.toString()) + ")");
						((DisplayActivity) getActivity()).initReceiptTask();
						((DisplayActivity) getActivity()).mReceiptTask.execute(
								expenseID, receiptPath);
					}
				} catch (Exception e) {
					Log.e("RECEIPT",
							"Error trying to initate UploadReceiptTask: "
									+ e.toString());
				}
				// report good news
				Toast.makeText(getActivity().getBaseContext(),
						"Expense Updated", Toast.LENGTH_SHORT).show();
				// callback the team id
				((DisplayActivity) getActivity()).refreshTeam(UserSession
						.getInstance(getActivity()).currentTeamID);
				getDialog().dismiss();
			} else {// some error occured
				Toast.makeText(getActivity().getBaseContext(),
						"Unable to Update Expense", Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			mEditTask = null;
		}
	}

}
