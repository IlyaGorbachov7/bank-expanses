import gorbachev.id.BelinvestBankInfo;
import gorbachev.id.BelinvestBankParser;
import gorbachev.id.core.DitailStatment;
import gorbachev.id.core.ResultParser;
import gorbachev.id.core.model.ParamParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

public class BelInvestBankTest {

	@Test
	void name() throws IOException {
		BelinvestBankParser parser = new BelinvestBankParser();
		ResultParser res = parser.parse(new ParamParser(
				Path.of("C:\\Users\\User\\Downloads\\belinvest.pdf").toFile(),
				LocalDate.of(2026, 2 ,1),
				LocalDate.of(2026, 5 ,2),
				DitailStatment.YEAR
		));

	}
}
