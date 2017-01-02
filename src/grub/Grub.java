package grub;

import java.util.Scanner;

public class Grub {
	public static void main(String[] args) {
		System.out.println("Hello Rachel, this is Grub, your personal RSS feed manager");
		Scanner scanner = new Scanner(System.in);
		
		while (true) {
			System.out.println("What can I do for you?");
			String request = scanner.nextLine();
			if (request.equals("q")) {
				System.out.println("Have a good day. Bye!");
				break;
			} else {
				Parser.parse(request);
			}
		}
	}
}
