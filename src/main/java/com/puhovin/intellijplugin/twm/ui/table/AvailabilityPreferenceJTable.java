package com.puhovin.intellijplugin.twm.ui.table;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.UIUtil;
import com.puhovin.intellijplugin.twm.model.AvailabilityPreference;
import org.jetbrains.annotations.NotNull;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class AvailabilityPreferenceJTable extends JBTable {
    public static final int TOOL_WINDOW_ID_COLUMN_INDEX = 0;
    public static final int AVAILABILITY_PREFERENCE_COLUMN_INDEX = 1;
    private final transient TableCellEditor availabilityPreferenceCellEditor;
    private final transient Project project;

    public AvailabilityPreferenceJTable(@NotNull Project project, TableModel model) {
        super(model);
        this.project = project;
        ComboBoxModel<AvailabilityPreference> boxModel = new DefaultComboBoxModel<>(AvailabilityPreference.values());
        availabilityPreferenceCellEditor = new DefaultCellEditor(new JComboBox<>(boxModel));
        setAutoCreateRowSorter(true);
        setRowHeight((int) (UIUtil.getLabelFont().getSize() * 1.8));
        getRowSorter().toggleSortOrder(0);
        createDefaultColumnsFromModel();
    }

    @Override
    public void createDefaultColumnsFromModel() {
        super.createDefaultColumnsFromModel();
        getColumn(getColumnName(TOOL_WINDOW_ID_COLUMN_INDEX)).setPreferredWidth(400);
        getColumn(getColumnName(AVAILABILITY_PREFERENCE_COLUMN_INDEX)).setPreferredWidth(200);
    }

    @Override
    protected TableModel createDefaultDataModel() {
        return new AvailabilityPreferenceTableModel();
    }

    @Override
    protected ListSelectionModel createDefaultSelectionModel() {
        final ListSelectionModel listSelectionModel = new DefaultListSelectionModel();

        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        return listSelectionModel;
    }

    @Override
    protected @NotNull JTableHeader createDefaultTableHeader() {
        final JTableHeader defaultTableHeader = super.createDefaultTableHeader();

        defaultTableHeader.setReorderingAllowed(false);
        defaultTableHeader.setBackground(UIUtil.getPanelBackground());

        return defaultTableHeader;
    }

    @Override
    public TableCellEditor getCellEditor(final int row, final int column) {
        final TableCellEditor result;

        if (column == AVAILABILITY_PREFERENCE_COLUMN_INDEX) {
            result = availabilityPreferenceCellEditor;
        } else {
            result = super.getCellEditor(row, column);
        }

        return result;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        final TableCellRenderer result;
        final AvailabilityPreference availabilityPreference =
                (AvailabilityPreference) getValueAt(row, AvailabilityPreferenceJTable.AVAILABILITY_PREFERENCE_COLUMN_INDEX);

        final AvailabilityPreference effectivePreference = (availabilityPreference != null) ? availabilityPreference : AvailabilityPreference.UNAFFECTED;

        result = switch (effectivePreference) {
            case AVAILABLE ->
                    new ToolWindowPreferenceCellRenderer(project, JBColor.namedColor("FileColor.Green", new JBColor(0xeffae7, 0x49544a)));
            case UNAFFECTED ->
                    new ToolWindowPreferenceCellRenderer(project, JBColor.WHITE);
            case UNAVAILABLE ->
                    new ToolWindowPreferenceCellRenderer(project, JBColor.namedColor("FileColor.Rose", new JBColor(0xf2dcda, 0x6e535b)));
        };

        return result;
    }
}
