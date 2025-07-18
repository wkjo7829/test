package gradeProgram;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

	private static final String url = "jdbc:mysql://localhost/gradeDb";
	private static final String id = "root";
	private static final String pw = "1234";

	private static Scanner sc = new Scanner(System.in); 

	private static String loggedInUserId = null;
	private static String loggedInUserRole = null;

	public static Connection getConnection() throws SQLException {

		return DriverManager.getConnection(url, id, pw);
	}

	public static void loadDriver() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			System.out.println("JDBC 드라이버 로드 성공");
		} catch (ClassNotFoundException e) {
			System.out.println("JDBC 드라이버 로드 실패!!!!!!!!: " + e.getMessage());
		}
	}

	public static void registerUser() {

		System.out.println("==== 회원가입 ====");
		System.out.print("아이디: ");
		String uId = sc.nextLine();
		System.out.print("비밀번호: ");
		String uPw = sc.nextLine();
		System.out.print("이름: ");
		String uName = sc.nextLine();
		System.out.print("역할 (student/teacher): ");
		String uRole = sc.nextLine();

		int uGrade = 0;
		int uClass = 0;

		if (uRole.equals("student")) {
			System.out.print("학년: ");
			uGrade = Integer.parseInt(sc.nextLine());
			System.out.print("반: ");
			uClass = Integer.parseInt(sc.nextLine());
		}

		try (Connection conn = getConnection()) {
			String sql = "INSERT INTO users (uId, uPw, uName, uRole, uGrade, uClass) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, uId);
			pstmt.setString(2, uPw);
			pstmt.setString(3, uName);
			pstmt.setString(4, uRole);
			pstmt.setInt(5, uGrade);
			pstmt.setInt(6, uClass);

			int result = pstmt.executeUpdate();
			System.out.println(result > 0 ? "회원가입 성공!" : "회원가입 실패...");

		} catch (SQLException e) {
			System.out.println("DB 오류: " + e.getMessage());
		}

	}

	public static void loginUser() {

		System.out.println("==== 로그인 ====");
		System.out.print("아이디: ");
		String uId = sc.nextLine();
		System.out.print("비밀번호: ");
		String uPw = sc.nextLine();

		try (Connection conn = getConnection()) {
			String sql = "SELECT uPw, uRole FROM users WHERE uId = ?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, uId);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				String dbPw = rs.getString("uPw");
				String uRole = rs.getString("uRole");

				if (dbPw.equals(uPw)) {
					System.out.println("로그인 성공! 역할: " + uRole);
					loggedInUserId = uId; // 예: 로그인한 사용자 아이디 저장
					loggedInUserRole = uRole; // 로그인한 사용자 역할 저장
				} else {
					System.out.println("비밀번호가 틀렸습니다.");
				}
			} else {
				System.out.println("존재하지 않는 아이디입니다.");
			}

		} catch (SQLException e) {
			System.out.println("DB 오류: " + e.getMessage());
		}

	}

	public static void logoutUser() {
		if (loggedInUserId != null) {
			System.out.println(loggedInUserId + "님이 로그아웃 되었습니다.");
			loggedInUserId = null;
			loggedInUserRole = null;
		} else {
			System.out.println("현재 로그인된 사용자가 없습니다.");
		}
	}

	public static void showMainMenu() { 
		while (loggedInUserId == null) { // 로그인 전이면 반복
			System.out.println("\n=== 메인 메뉴 ===");
			System.out.println("1. 회원가입");
			System.out.println("2. 로그인");
			System.out.println("3. 프로그램 종료");
			System.out.print("선택: ");
			String choice = sc.nextLine();

			switch (choice) {
			case "1":
				registerUser();
				break;
			case "2":
				loginUser();
				break;
			case "3":
				System.out.println("프로그램을 종료합니다.");
				// sc.close();
				System.exit(0);
			default:
				System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
			}
		}
	}

	public static void showStudentScores() {
		try (Connection conn = getConnection()) {
			String sql = "SELECT uKorean_score, uEnglish_score, uMath_score, uScience_score, uTotal_score, uAverage_score, uRank "
					+ "FROM users WHERE uId = ?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, loggedInUserId);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				System.out.println("=== 본인 점수 정보 ===");
				System.out.println("국어: " + rs.getInt("uKorean_score"));
				System.out.println("영어: " + rs.getInt("uEnglish_score"));
				System.out.println("수학: " + rs.getInt("uMath_score"));
				System.out.println("과학: " + rs.getInt("uScience_score"));
				System.out.println("총점: " + rs.getInt("uTotal_score"));
				System.out.printf("평균: %.2f\n", rs.getDouble("uAverage_score"));
				System.out.println("등수: " + rs.getInt("uRank"));
			} else {
				System.out.println("학생 정보를 찾을 수 없습니다.");
			}
		} catch (SQLException e) {
			System.out.println("DB 오류: " + e.getMessage());
		}
	}

	public static void inputStudentScores() {

		System.out.println("=== 학생 점수 입력 ===");
		System.out.print("학생 아이디 입력: ");
		String studentId = sc.nextLine();

		try (Connection conn = getConnection()) {
			// 학생 존재 여부 확인
			String checkSql = "SELECT uName FROM users WHERE uId = ? AND uRole = 'student'";
			PreparedStatement checkStmt = conn.prepareStatement(checkSql);
			checkStmt.setString(1, studentId);
			ResultSet checkRs = checkStmt.executeQuery();

			if (!checkRs.next()) {
				System.out.println("해당 아이디의 학생이 존재하지 않습니다.");
				return;
			}

			System.out.print("국어 점수: ");
			int kor = Integer.parseInt(sc.nextLine());
			System.out.print("영어 점수: ");
			int eng = Integer.parseInt(sc.nextLine());
			System.out.print("수학 점수: ");
			int math = Integer.parseInt(sc.nextLine());
			System.out.print("과학 점수: ");
			int sci = Integer.parseInt(sc.nextLine());

			int total = kor + eng + math + sci;
			double avg = total / 4.0;

			int rank = 0;

			String updateSql = "UPDATE users SET uKorean_score=?, uEnglish_score=?, uMath_score=?, uScience_score=?, "
					+ "uTotal_score=?, uAverage_score=?, uRank=? WHERE uId=?";

			PreparedStatement updateStmt = conn.prepareStatement(updateSql);
			updateStmt.setInt(1, kor);
			updateStmt.setInt(2, eng);
			updateStmt.setInt(3, math);
			updateStmt.setInt(4, sci);
			updateStmt.setInt(5, total);
			updateStmt.setDouble(6, avg);
			updateStmt.setInt(7, rank);
			updateStmt.setString(8, studentId);

			int res = updateStmt.executeUpdate();
			System.out.println(res > 0 ? "점수 입력 완료." : "점수 입력 실패.");

			if (res > 0) {
			    String rankUpdateSql =
			        "UPDATE users u " +
			        "JOIN ( " +
			        "    SELECT uN, RANK() OVER (ORDER BY uTotal_score DESC) AS `rank` " +
			        "    FROM users " +
			        "    WHERE uRole = 'student'  " +
			        ") r ON u.uN = r.uN " +
			        "SET u.uRank = r.`rank`" +
			        "WHERE u.uRole = 'student'";

			    PreparedStatement rankStmt = conn.prepareStatement(rankUpdateSql);
			    int rankUpdated = rankStmt.executeUpdate();
			    System.out.println("등수 업데이트 완료 (" + rankUpdated + "명 반영됨)");
			}
			
		} catch (SQLException e) {
			System.out.println("DB 오류: " + e.getMessage());
		}
	}

	public static void showUserMenu() {

		while (true) {
			System.out.println("\n=== " + loggedInUserRole + " 메뉴 ===");
			if ("student".equals(loggedInUserRole)) {
				System.out.println("1. 본인 점수 조회");
				System.out.println("2. 로그아웃");
				System.out.print("선택: ");

				String choice = sc.nextLine();
				switch (choice) {
				case "1":
					showStudentScores();
					break;
				case "2":
					logoutUser();
					return; 
				default:
					System.out.println("잘못된 선택입니다.");
				}
			} else if ("teacher".equals(loggedInUserRole)) {
				System.out.println("1. 학생 점수 입력");
				System.out.println("2. 로그아웃");
				System.out.print("선택: ");

				String choice = sc.nextLine();
				switch (choice) {
				case "1":
					inputStudentScores();
					break;
				case "2":
					logoutUser();
					return;
				default:
					System.out.println("잘못된 선택입니다.");
				}
			} else {
				System.out.println("알 수 없는 역할입니다. 로그아웃 처리합니다.");
				logoutUser();
				return;
			}
		}
	}

	public static void main(String[] args) {
		loadDriver();
		while (true) {
			showMainMenu();
			showUserMenu();
		}

	}

}
