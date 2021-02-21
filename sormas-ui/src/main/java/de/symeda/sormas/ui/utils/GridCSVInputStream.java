package de.symeda.sormas.ui.utils;

import com.opencsv.CSVWriter;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.AgeAndBirthDateDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.person.PersonHelper;
import de.symeda.sormas.api.utils.CSVUtils;
import de.symeda.sormas.api.utils.YesNoUnknown;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class GridCSVInputStream extends InputStream {
    final static int ROWS_IN_MEMORY = 100;

    final Grid<?> grid;
    final List<String> excludePropertyIdsList;
    final List<String> includePropertyIdsList;
    DataProvider<?, ?> dataProvider;
    ValueProvider[] columnValueProviders;
    String[] rowValues;
    int totalRowCount;
    ByteArrayOutputStream byteArrayOutputStream;
    CSVWriter writer = null;
    List<QuerySortOrder> sortOrder = null;
    int currentRow = 0;
    int currentByte = 0;
    byte[] byteArray = null;
    ByteArrayInputStream byteArrayInputStream = null;

    public GridCSVInputStream(Grid<?> grid) {
        this(grid, Collections.emptyList(), Collections.emptyList());
    }

    public GridCSVInputStream(Grid<?> grid, List<String> excludePropertyIdsList) {
        this(grid, excludePropertyIdsList, Collections.emptyList());
    }

    public GridCSVInputStream(Grid<?> grid, List<String> excludePropertyIdsList, List<String> includePropertyIdsList) {
        this.grid = grid;
        this.excludePropertyIdsList = new ArrayList<>(excludePropertyIdsList);
        this.includePropertyIdsList = new ArrayList<>(includePropertyIdsList);
        currentRow = 0;
        currentByte = 0;
    }

    @Override
    public int read() {
        if (currentRow == 0) {
            String[] headerRow;
            {
                List<Grid.Column> columns = grid.getColumns()
                        .stream()
                        .filter(c -> !c.isHidden())
                        .filter(c -> !c.isHidden() || includePropertyIdsList.contains(c.getId()))
                        .filter(c -> !excludePropertyIdsList.contains(c.getId()))
                        .collect(Collectors.toList());

                columnValueProviders = columns.stream().map(Grid.Column::getValueProvider).toArray(ValueProvider[]::new);

                headerRow = columns.stream().map(c -> c.getCaption()).toArray(String[]::new);
                String[] rowValues = new String[columnValueProviders.length];
            }

            DataProvider<?, ?> dataProvider = grid.getDataProvider();

            sortOrder = grid.getSortOrder()
                    .stream()
                    .flatMap(
                            gridSortOrder -> grid.getColumns()
                                    .stream()
                                    .filter(column -> column.getId().equals(gridSortOrder.getSorted().getId()))
                                    .findFirst()
                                    .get()
                                    .getSortOrder(gridSortOrder.getDirection()))
                    .collect(Collectors.toList());

            byteArrayOutputStream = new ByteArrayOutputStream();
            writer = CSVUtils.createCSVWriter(
                    new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8),
                    FacadeProvider.getConfigFacade().getCsvSeparator());
            totalRowCount = dataProvider.size(new Query());
            writer.writeNext(headerRow);
            try {
                fetchRow(currentRow); currentRow += ROWS_IN_MEMORY;
                byteArray = byteArrayOutputStream.toByteArray();
                byteArrayInputStream = new ByteArrayInputStream(byteArray);
                return byteArrayInputStream.read();
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
        } else {
            int b = byteArrayInputStream.read();
            if (b == -1) {
                if (currentRow < totalRowCount) {
                    try {
                        fetchRow(currentRow); currentRow += ROWS_IN_MEMORY;
                        byteArray = byteArrayOutputStream.toByteArray();
                        byteArrayInputStream = new ByteArrayInputStream(byteArray);
                        return byteArrayInputStream.read();
                    } catch (IOException ioException) {
                        throw new RuntimeException(ioException);
                    }
                } else {
                    return -1;
                }
            } else {
                return b;
            }
        }
    }

    private void fetchRow(int i) throws IOException {
        dataProvider.fetch(new Query(i, ROWS_IN_MEMORY, sortOrder, null, null)).forEach(row -> {
            for (int c = 0; c < columnValueProviders.length; c++) {
                Object value = columnValueProviders[c].apply(row);

                final String valueString;
                if (value == null) {
                    valueString = "";
                } else if (value instanceof Date) {
                    valueString = DateFormatHelper.formatLocalDateTime((Date) value);
                } else if (value instanceof Boolean) {
                    if ((Boolean) value == true) {
                        valueString = I18nProperties.getEnumCaption(YesNoUnknown.YES);
                    } else
                        valueString = I18nProperties.getEnumCaption(YesNoUnknown.NO);
                } else if (value instanceof AgeAndBirthDateDto) {
                    AgeAndBirthDateDto ageAndBirthDate = (AgeAndBirthDateDto) value;
                    valueString = PersonHelper.getAgeAndBirthdateString(
                            ageAndBirthDate.getAge(),
                            ageAndBirthDate.getAgeType(),
                            ageAndBirthDate.getBirthdateDD(),
                            ageAndBirthDate.getBirthdateMM(),
                            ageAndBirthDate.getBirthdateYYYY(),
                            I18nProperties.getUserLanguage());
                } else if (value instanceof Label) {
                    valueString = ((Label) value).getValue();
                } else {
                    valueString = value.toString();
                }
                rowValues[c] = valueString;
            }
            writer.writeNext(rowValues);
        });
        writer.flush();
    }
}
