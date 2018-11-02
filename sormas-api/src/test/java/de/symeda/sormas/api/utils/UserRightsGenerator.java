package de.symeda.sormas.api.utils;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.junit.Test;

import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;

public class UserRightsGenerator {
	
	@Test
	public void generateUserRights() throws FileNotFoundException, IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		
		// Create User Rights sheet
		String safeName = WorkbookUtil.createSafeSheetName("User Rights");
		XSSFSheet sheet = workbook.createSheet(safeName);
		
		// Initialize cell styles
		// Authorized style
		XSSFCellStyle authorizedStyle = workbook.createCellStyle();
	    authorizedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		authorizedStyle.setFillForegroundColor(new XSSFColor(new Color(0, 153, 0)));
		authorizedStyle.setBorderBottom(BorderStyle.THIN);
		authorizedStyle.setBorderLeft(BorderStyle.THIN);
		authorizedStyle.setBorderTop(BorderStyle.THIN);
		authorizedStyle.setBorderRight(BorderStyle.THIN);
		authorizedStyle.setBorderColor(BorderSide.BOTTOM, new XSSFColor(Color.BLACK));
		authorizedStyle.setBorderColor(BorderSide.LEFT, new XSSFColor(Color.BLACK));
		authorizedStyle.setBorderColor(BorderSide.TOP, new XSSFColor(Color.BLACK));
		authorizedStyle.setBorderColor(BorderSide.RIGHT, new XSSFColor(Color.BLACK));
		// Unauthorized style
		XSSFCellStyle unauthorizedStyle = workbook.createCellStyle();
		unauthorizedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		unauthorizedStyle.setFillForegroundColor(new XSSFColor(Color.RED));
		unauthorizedStyle.setBorderBottom(BorderStyle.THIN);
		unauthorizedStyle.setBorderLeft(BorderStyle.THIN);
		unauthorizedStyle.setBorderTop(BorderStyle.THIN);
		unauthorizedStyle.setBorderRight(BorderStyle.THIN);
		unauthorizedStyle.setBorderColor(BorderSide.BOTTOM, new XSSFColor(Color.BLACK));
		unauthorizedStyle.setBorderColor(BorderSide.LEFT, new XSSFColor(Color.BLACK));
		unauthorizedStyle.setBorderColor(BorderSide.TOP, new XSSFColor(Color.BLACK));
		unauthorizedStyle.setBorderColor(BorderSide.RIGHT, new XSSFColor(Color.BLACK));
		// Bold style
		XSSFFont boldFont = workbook.createFont();
		boldFont.setBold(true);
		XSSFCellStyle boldStyle = workbook.createCellStyle();
		boldStyle.setFont(boldFont);

		int rowCounter = 0;
		
		// Header
		Row headerRow = sheet.createRow(rowCounter++);
		Cell userRightHeadlineCell = headerRow.createCell(0);
		userRightHeadlineCell.setCellValue("User Right");
		userRightHeadlineCell.setCellStyle(boldStyle);
		Cell descHeadlineCell = headerRow.createCell(1);
		descHeadlineCell.setCellValue("Description");
		descHeadlineCell.setCellStyle(boldStyle);
		sheet.setColumnWidth(0, 256 * 35);
		sheet.setColumnWidth(1, 256 * 50);
		for (UserRole userRole : UserRole.values()) {
			String columnCaption = userRole.toString();
			Cell headerCell = headerRow.createCell(userRole.ordinal() + 2);
			headerCell.setCellValue(columnCaption);
			headerCell.setCellStyle(boldStyle);
			sheet.setColumnWidth(userRole.ordinal() + 2, 256 * 14);
		}
		
		// User right rows
		for (UserRight userRight : UserRight.values()) {
			Row row = sheet.createRow(rowCounter++);
			
			// User right name
			Cell nameCell = row.createCell(0);
			nameCell.setCellValue(userRight.name());
			nameCell.setCellStyle(boldStyle);
			
			// User right description
			Cell descCell = row.createCell(1);
			descCell.setCellValue(userRight.toString());
			
			// Add styled cells for all user roles
			for (UserRole userRole : UserRole.values()) {
				Cell roleRightCell = row.createCell(userRole.ordinal() + 2);
				if (userRole.hasRight(userRight)) {
					roleRightCell.setCellStyle(authorizedStyle);
					roleRightCell.setCellValue("Yes");
				} else {
					roleRightCell.setCellStyle(unauthorizedStyle);
					roleRightCell.setCellValue("No");
				}
			}
		}
		
		String filePath = "../sormas-base/doc/SormasUserRights.xlsx";
		try (OutputStream fileOut = new FileOutputStream(filePath)) {
			workbook.write(fileOut);
		}
		
		Desktop.getDesktop().open(new File(filePath));
		workbook.close();
	}

}
