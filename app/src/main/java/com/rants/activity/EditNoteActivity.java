package com.rants.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.rants.R;
import com.rants.data.Note;
import com.rants.data.dao.NoteDAO;
import com.rants.util.Strings;
import com.rants.widget.NotesAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Actividad para editar notas.
 *
 * @author Daniel Pedraza Arcega
 */
@ContentView(R.layout.activity_edit_note)
public class EditNoteActivity extends RoboActionBarActivity {

    private static final String EXTRA_NOTE = "EXTRA_NOTE";
    private static final int EDIT_NOTE_RESULT_CODE = 8;
    private static final String EXTRA_UPDATED_NOTE = "EXTRA_UPDATED_NOTE";

    @InjectView(R.id.note_content) private MultiAutoCompleteTextView noteContentText;
    @Inject private NoteDAO noteDAO;
    private ArrayList<NotesAdapter.NoteViewWrapper> notesData;

    private Note note;
    public int maxSize=140;

    /**
     * Construye el Intent para llamar a esta actividad con una nota ya existente.
     *
     * @param context el contexto que la llama.
     * @param note la nota a editar.
     * @return un Intent.
     */
    public static Intent buildIntent(Context context, Note note) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra(EXTRA_NOTE, note);
        return intent;
    }

    public static Note getExtraUpdatedNote(Intent intent) {
        return (Note) intent.getExtras().get(EXTRA_UPDATED_NOTE);
    }

    /**
     * Construye el Intent para llamar a esta actividad para crear una nota.
     *
     * @param context el contexto que la llama.
     * @return un Intent.
     */
    public static Intent buildIntent(Context context) {
        return buildIntent(context, null);
    }

    /**
     * Recupera la nota editada.
     *
     * @param intent el Intent que vine en onActivityResult
     * @return la nota actualizada
     */
    public static Note getExtraNote(Intent intent) {
        return (Note) intent.getExtras().get(EXTRA_NOTE);
    }

    /** {@inheritDoc} */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		// Inicializa los componentes //////////////////////////////////////////////////////////////
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Muestra la flecha hacia atrás
        note = (Note) getIntent().getSerializableExtra(EXTRA_NOTE); // Recuperar la nota del Intent
        if (note != null) { // Editar nota existente
            SpannableString hashText = new SpannableString(note.getContent());
            Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
            while (matcher.find())
            {
                hashText.setSpan(new ForegroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), 0);
            }
            noteContentText.setText(hashText);
        } else { // Nueva nota
            note = new Note();
            note.setCreatedAt(new Date());
        }

        Button btnSave = (Button) findViewById(R.id.btn_save);
        Button btnHash = (Button) findViewById(R.id.btn_hash);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNoteOrShowToast();
            }
        });

        btnHash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiAutoCompleteTextView editText = (MultiAutoCompleteTextView) findViewById(R.id.note_content);
                editText.getText().insert(editText.getSelectionStart(), "#");
            }
        });

        notesData = new ArrayList<>();
        Set hashtags= new HashSet();
        for (Note note : noteDAO.fetchAll()) { // Convertir a wrapper
            String content = note.getContent();

            String c[];
            if(content.contains("#")){
                c = content.split("\\s+");

                for(String element:c){
                    if(element.startsWith("#") && element.length()>1){
                        hashtags.add(element.replaceAll("[^a-zA-Z#0-9 ]", ""));

                    }
                }
            }
        }
        String[] tags =  new String[hashtags.size()];
        Iterator<String> iter = hashtags.iterator();

        int i=0;
        while(iter.hasNext()){
            tags[i++]= iter.next();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,tags);

        noteContentText.setRawInputType(InputType.TYPE_CLASS_TEXT
                |InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                |InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        noteContentText.setTokenizer(new SpaceTokenizer());
        noteContentText.setAdapter(adapter);
    }

    /** {@inheritDoc} */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    /** @return {@code true} si tiene titulo y contenido; {@code false} en cualquier otro caso. */
    private boolean isNoteFormOk() {
        String content=noteContentText.getText().toString();
       return  !Strings.isNullOrBlank(content) && content.length()<=maxSize;
    }

    /**
     * Actualiza el contenido del objeto Note con los campos de texto del layout y pone el objeto
     * como resultado de esta actividad.
     */
    private void setNoteResult() {
        note.setContent(noteContentText.getText().toString().trim());
            note.setUpdatedAt(new Date());
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_NOTE, note);
            setResult(RESULT_OK, resultIntent);


    }

    /** Muestra mensajes de validación de la forma de la nota. */
    private void validateNoteForm() {
        String message = null;
        String noteContent = noteContentText.getText().toString();
        if (Strings.isNullOrBlank(noteContent)) {
            message = getString(R.string.content_required);
        }

        if(noteContent.length()>maxSize){
            message = getString(R.string.content_less_than_140);
        }

        if (message != null) {
            Toast.makeText(getApplicationContext(), message,Toast.LENGTH_LONG).show();
        }
    }

    private void AddNoteOrShowToast(){
         if (isNoteFormOk()) {
             setNoteResult();
             finish();
        } else validateNoteForm();
     }

    /** {@inheritDoc} */
    @Override
    public void onBackPressed() {
        // No se edito ningúna nota ni creo alguna nota
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

    public class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {

        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;

            while (i > 0 && text.charAt(i - 1) != ' ') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }

            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (text.charAt(i) == ' ') {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();

            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }

            if (i > 0 && text.charAt(i - 1) == ' ') {
                return text;
            } else {
                if (text instanceof Spanned) {
                    SpannableString sp = new SpannableString(text + " ");
                    TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                            Object.class, sp, 0);
                    return sp;
                } else {
                    return text + " ";
                }
            }
        }
    }

}
