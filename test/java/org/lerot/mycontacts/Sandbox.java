package org.lerot.mycontacts;

import java.nio.file.Files;
import java.nio.file.Path;

/*
 * ./gradlew runSandbox: MyContacts on a copy of the made-up test data in
 * build/sandbox, to try things (an import, say) without touching the real
 * contacts. The copy is kept between runs; ./gradlew runSandbox -Preset
 * starts it afresh.
 */
public class Sandbox
{
	public static void main(String[] args) throws Exception
	{
		Path home = Path.of(args[0]);
		boolean reset = args.length > 1 && args[1].equals("reset");
		if (reset || !Files.exists(home.resolve(".mccontacts"))) Screenshots.makeHome(home);
		System.setProperty("user.home", home.toString());
		System.out.println("sandbox: " + home);
		Main.main(new String[0]);
	}
}
