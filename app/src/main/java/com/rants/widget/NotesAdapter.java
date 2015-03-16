package com.rants.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.rants.R;
import com.rants.data.Note;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Adaptador de notas. Actua como intermediario entre la vista y los datos.
 *
 * @author Daniel Pedraza Arcega
 * @see <a href="http://bit.ly/1vZt3ny">Building Layouts with an Adapter</a>
 */
public class NotesAdapter extends BaseAdapter implements Filterable{

    private static final DateFormat DATETIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    private final List<NoteViewWrapper> data;
    private List<NoteViewWrapper> filtered_data;


     /** Wrapper para notas. Util para cambiar el fondo de los item seleccionados. */
    public static class NoteViewWrapper {

        private final Note note;
        private boolean isSelected;

        /**
         * Contruye un nuevo NoteWrapper con la nota dada.
         *
         * @param note una nota.
         */
        public NoteViewWrapper(Note note) {
            this.note = note;
        }

        public Note getNote() {
            return note;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }
    }



    /**
     * Constructor.
     *
     * @param data la lista de notas a usar como fuente de datos para este adaptador.
     */
    public NotesAdapter(List<NoteViewWrapper> data) {
        this.data = data;
        this.filtered_data = data;
    }

    /** @return cuantos datos hay en la lista de notas. */
    @Override
    public int getCount() {
        return filtered_data.size();
    }

    /**
     * @param position la posición de la nota que se quiere
     * @return la nota en la posición dada.
     */
    @Override
    public NoteViewWrapper getItem(int position) {
        return filtered_data.get(position);
    }

    /**
     * @param position una posición
     * @return la misma posición dada
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Muestra los datos de la nota en la posición dada en una instancia del componente visual
     * {@link com.rants.R.layout#notes_row}.
     *
     * @see <a href="http://bit.ly/MJqzXb">Hold View Objects in a View Holder</a>
     * @param position la posición de la nota en curso.
     * @param convertView el componente visual a usar.
     * @param parent el componente visual padre del componente visual a usar.
     * @return la vista con los datos.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) { // inflar componente visual
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_row, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else holder = (ViewHolder) convertView.getTag(); // ya existe, solo es reciclarlo
        // Inicializa la vista con los datos de la nota

        NoteViewWrapper noteViewWrapper = filtered_data.get(position);
        holder.noteIdText.setText(String.valueOf(noteViewWrapper.note.getId()));
        // Corta la cadena a 80 caracteres y le agrega "..."

        SpannableString hashText = new SpannableString(noteViewWrapper.note.getContent());
        Matcher matcher = Pattern.compile("#([A-Za-z0-9_-]+)").matcher(hashText);
        while (matcher.find())
        {
            hashText.setSpan(new ForegroundColorSpan(Color.BLUE), matcher.start(), matcher.end(), 0);
        }


        holder.noteContentText.setText(hashText);

        holder.noteDateText.setText(DATETIME_FORMAT.format(noteViewWrapper.note.getUpdatedAt()));
        // Cambia el color del fondo si es seleccionado
        if (noteViewWrapper.isSelected) holder.parent.setBackgroundColor(parent.getContext().getResources().getColor(R.color.selected_note));
        // Sino lo regresa a transparente
        else holder.parent.setBackgroundColor(parent.getContext().getResources().getColor(android.R.color.transparent));
        return convertView;
    }

    /** Almacena componentes visuales para acceso rápido sin necesidad de buscarlos muy seguido.*/
    private static class ViewHolder {

        private TextView noteIdText;
        private TextView noteContentText;
        private TextView noteDateText;

        private View parent;

        /**
         * Constructor. Encuentra todas los componentes visuales en el componente padre dado.
         *
         * @param parent un componente visual.
         */
        private ViewHolder(View parent) {
            this.parent = parent;
            noteIdText = (TextView) parent.findViewById(R.id.note_id);
            noteContentText = (TextView) parent.findViewById(R.id.note_content);
            noteDateText = (TextView) parent.findViewById(R.id.note_date);
        }
    }

@Override
public Filter getFilter() {
Filter filter = new Filter(){

    @Override
    protected FilterResults performFiltering(CharSequence query) {
        FilterResults results = new FilterResults();
        ArrayList<NoteViewWrapper> filtered = new ArrayList<>();
        query = query.toString().toLowerCase();
        if(query!=null && query.length()>0){
            for(int i=0;i<data.size();i++){
                String dataName = data.get(i).getNote().getContent().toLowerCase();
                if (dataName.contains(query)){
                    filtered.add(data.get(i));
                }

            }
            results.count = filtered.size();
            results.values = filtered;

        }else{
            results.count = data.size();
            results.values = data;
        }

        Log.d("publishing", results.values.toString());
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        filtered_data =  (ArrayList<NoteViewWrapper>) results.values;
        NotesAdapter.this.notifyDataSetChanged();
    }
};

return filter;
}
}