import org.hyperskill.hstest.dynamic.output.InfiniteLoopDetector;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testcase.SimpleTestCase;
import org.hyperskill.hstest.testcase.TestCase;
import org.hyperskill.hstest.testing.TestedProgram;
import org.hyperskill.hstest.testing.execution.MainMethodExecutor;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SharedBillsSplitterTestStage6 extends StageTest {

    static {
        InfiniteLoopDetector.setWorking(false);
    }

    private static final String UNKNOWN_COMMAND = "Unknown command";
    private static final String EXIT_ERROR = "Your program should stop after exit command";
    private static final String HELP_ERROR = "Help command should print all commands line by line in natural order";
    private static final String ILLEGAL_COMMAND_ARGUMENTS = "Illegal command arguments";
    private static final String ILLEGAL_ARGUMENTS_ERROR = "Your program should handle exceptions in incorrect command arguments input";
    private static final String UNKNOWN_GROUP = "Unknown group";
    private static final String NO_REPAYMENTS_NEED = "No repayments need";
    private static final String WRONG_CALCULATIONS = "Wrong calculations. Program should output owes list that " +
        "if every person in this list repay his owes then everyone will have zero balance and everyone will be paid off";
    private static final String GIFT_TO = " gift to ";
    private static final String GROUP_PERSONS_FEEDBACK = "Persons in group should be printed line by line sorted in ascending order";
    private static final String BALANCE_OWES_FEEDBACK = "Owes should be sorted by Person who owes and Person whom owes";
    private static final String WRITE_OFF_FEEDBACK = "WriteOff should remove from storage all money operations till command date";
    public static final String FILTERED_BALANCE_FEEDBACK = "Program should output balance result for persons who contains if filter. However owes values should be the same as if balance were unfiltered.";
    public static final String BALANCE_PERFECT_FEEDBACK = "Your program should reduce repayments count for balancePerfect";

    private final String databasePath;

    public SharedBillsSplitterTestStage6() {
        databasePath = "../testDB" + ".mv.db";
    }

    enum Commands {
        help,
        borrow,
        repay,
        balance,
        exit,
        group,
        purchase,
        secretSanta,
        cashBack,
        writeOff,
        balancePerfect
    }

    @Before
    public void doSomeBefore() {
        if (databasePath != null) {
            replaceDatabase();
        }
    }

    @After
    public void doSomeAfter() {
        if (databasePath != null) {
            revertDatabase();
        }
    }

    private void replaceDatabase() {
        String dbFilePath = System.getProperty("user.dir")
            + File.separator + databasePath;

        String dbTempFilePath = dbFilePath + "-real";

        Path dbFile = Paths.get(dbFilePath);
        Path dbTempFile = Paths.get(dbTempFilePath);

        try {
            if (dbTempFile.toFile().exists()) {
                Files.deleteIfExists(dbFile);
            } else if (dbFile.toFile().exists() && !dbTempFile.toFile().exists()) {
                Files.move(dbFile, dbTempFile);
            }
        } catch (IOException ignored) {
        }
    }

    private void revertDatabase() {
        String dbFilePath = System.getProperty("user.dir")
            + File.separator + databasePath;

        String dbTempFilePath = dbFilePath + "-real";

        Path dbFile = Paths.get(dbFilePath);
        Path dbTempFile = Paths.get(dbTempFilePath);

        try {
            Files.deleteIfExists(dbFile);
            if (dbTempFile.toFile().isFile()) {
                Files.move(dbTempFile, dbFile);
            }
        } catch (IOException ignored) {
        }
    }

    private <T> TestCase<T> caseToDynamicTesting(TestCase<T> testCase) {
        String feedback = testCase.getFeedback();
        String input = testCase.getInput();
        T attach = testCase.getAttach();
        BiFunction<String, T, CheckResult> checkFunc = testCase.getCheckFunc();

        return new TestCase<T>()
            .setFeedback(feedback)
            .setDynamicTesting(() -> {
                TestedProgram p = new TestedProgram();
                ((MainMethodExecutor) p.getProgramExecutor()).setUseSeparateClassLoader(false);
                p.setReturnOutputAfterExecution(false);
                p.start();
                p.execute(input);
                return checkFunc.apply(p.getOutput(), attach);
            });
    }

    @Override
    public List<TestCase> generate() {
        return List.of(

            caseToDynamicTesting(new TestCase<String>()
                .setCheckFunc(this::checkUnknownCommand)
                .setAttach("someAttachText")
                .setInput("someRandomText\n" +
                    "exit")),

            caseToDynamicTesting(new SimpleTestCase("" +
                "repay Ann\n" +
                "exit",
                ILLEGAL_COMMAND_ARGUMENTS)
                .setFeedback(ILLEGAL_ARGUMENTS_ERROR)),

            caseToDynamicTesting(new TestCase<String>()
                .setCheckFunc(this::checkHelpCommand)
                .setInput(concatLines(Commands.help.toString(), Commands.exit.toString()))),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute(Commands.exit.toString());
                if (!main.isFinished()) {
                    return CheckResult.wrong(EXIT_ERROR);
                }
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute("borrow Ann Bob 1.00");
                final String FUTURE_DATE = "3030.03.30";
                main.execute(FUTURE_DATE + " purchase Bob coffee 3.50 (Bob, Ann)");
                main.execute("writeOff");
                { //1st
                    String result = main.execute("balance close");
                    if (!result.startsWith(NO_REPAYMENTS_NEED)) {
                        return CheckResult.wrong(WRITE_OFF_FEEDBACK);
                    }
                }
                { //2nd
                    main.execute(FUTURE_DATE + " writeOff");
                    String result = main.execute(FUTURE_DATE + " balance close");
                    if (!result.startsWith(NO_REPAYMENTS_NEED)) {
                        return CheckResult.wrong(WRITE_OFF_FEEDBACK);
                    }
                }
                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                {
                    TestedProgram main = new TestedProgram();
                    ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                    main.start();
                    main.execute("writeOff");
                    main.execute("group create TEAM (Diana, Elon, Bob, Ann)");
                    main.execute("purchase Elon icecream 4.80 (TEAM)");
                    main.execute("borrow Ann Bob 1.05");
                    main.execute("repay Ann Bob 5.01");
                    main.execute("exit");
                }
                {
                    TestedProgram main = new TestedProgram();
                    ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                    main.start();
                    String groupResult = main.execute("group show TEAM");
                    if (!equalsByLines(groupResult, concatLines("Ann", "Bob", "Diana", "Elon"))) {
                        return CheckResult.wrong(GROUP_PERSONS_FEEDBACK + ". Also person and group should be stored in database");
                    }
                    String balanceResult = main.execute("balance close");
                    if (!equalsByLines(balanceResult, "Ann owes Elon 1.20\n" +
                        "Bob owes Ann 3.96\n" +
                        "Bob owes Elon 1.20\n" +
                        "Diana owes Elon 1.20")) {
                        return CheckResult.wrong(BALANCE_OWES_FEEDBACK + " Also all payment operations should be stored in database");
                    }
                    main.execute("exit");
                }
                return CheckResult.correct();
            }),

            caseToDynamicTesting(new SimpleTestCase(
                concatLines("writeOff",
                    "2020.09.30 borrow Ann Bob 20.10",
                    "2020.10.01 repay Ann Bob 10.22",
                    "2020.10.10 borrow Bob Ann 7.35",
                    "2020.10.15 repay Ann Bob 8.99",
                    "repay Bob Ann 6.46",
                    "2020.09.25 balance",
                    "2020.10.30 balance open",
                    "2020.10.20 balance close",
                    "balance close",
                    "exit"),
                concatLines(
                    NO_REPAYMENTS_NEED,
                    "Ann owes Bob 20.10",
                    "Bob owes Ann 6.46",
                    NO_REPAYMENTS_NEED)
            ).setFeedback("Money should be formatted with properly scale")),

            caseToDynamicTesting(new SimpleTestCase(
                concatLines("writeOff",
                    "borrow Ann Bob 25",
                    "repay Ann Bob 15",
                    "repay Bob Chuck 7",
                    "borrow Ann Bob 4",
                    "repay Bob Diana 5",
                    "borrow Elon Diana 12",
                    "repay Chuck Diana 14",
                    "repay Chuck Diana 12",
                    "balance close",
                    "exit"),
                concatLines(
                    "Ann owes Bob 14.00",
                    "Chuck owes Bob 7.00",
                    "Diana owes Bob 5.00",
                    "Diana owes Chuck 26.00",
                    "Elon owes Diana 12.00")
            ).setFeedback(BALANCE_OWES_FEEDBACK)),

            new TestCase().setDynamicTesting(() -> {
                Random random = new Random();
                List<String> persons = List.of("Annabelle", "Billibob", "Carlos", "Diana", "Elon", "Finny");
                String keyPerson = persons.get(random.nextInt(persons.size()));
                BigDecimal keyBalanceBorrow = BigDecimal.ZERO;
                BigDecimal keyBalanceRepay = BigDecimal.ZERO;
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute("writeOff");
                for (int i = 0; i < 100; i++) {
                    String personFrom = persons.get(random.nextInt(persons.size()));
                    String personTo = persons.get(random.nextInt(persons.size()));
                    if (personFrom.equalsIgnoreCase(personTo)) {
                        continue;
                    }
                    Commands command;
                    BigDecimal amount = new BigDecimal(String.format("%d.%d", random.nextInt(200), random.nextInt(99)));
                    if (random.nextBoolean()) {
                        command = Commands.borrow;
                        if (personFrom.equals(keyPerson)) {
                            keyBalanceBorrow = keyBalanceBorrow.add(amount);
                        }
                        if (personTo.equals(keyPerson)) {
                            keyBalanceBorrow = keyBalanceBorrow.subtract(amount);
                        }
                    } else {
                        command = Commands.repay;
                        if (personFrom.equals(keyPerson)) {
                            keyBalanceRepay = keyBalanceRepay.add(amount);
                        }
                        if (personTo.equals(keyPerson)) {
                            keyBalanceRepay = keyBalanceRepay.subtract(amount);
                        }
                    }
                    String line = String.format("%s %s %s %s", command, personFrom, personTo, amount);
                    main.execute(line);
                }
                String result = main.execute("balance close");
                Optional<BigDecimal> sum = Arrays.stream(result.split("\n"))
                    .filter(it -> it.contains(keyPerson))
                    .map(it -> {
                        String[] split = it.split("\\s+");
                        Character sign = it.startsWith(keyPerson) ? '+' : '-';
                        return sign + split[split.length - 1];
                    })
                    .map(BigDecimal::new)
                    .reduce(BigDecimal::add);

                BigDecimal sumBalance = keyBalanceBorrow.subtract(keyBalanceRepay);
                if (sumBalance.compareTo(sum.orElse(BigDecimal.ZERO)) == 0) {
                    main.execute("exit");
                    return CheckResult.correct();
                }
                return CheckResult.wrong(WRONG_CALCULATIONS);
            }),


            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                if (!main.execute("group create lowerCaseText").contains(ILLEGAL_COMMAND_ARGUMENTS)) {
                    return CheckResult.wrong(String.format("Group name must be UPPERCASE, otherwise \"%s\" should be printed",
                        ILLEGAL_COMMAND_ARGUMENTS));
                }
                if (!main.execute("group show NOTFOUNDGROUP").contains(UNKNOWN_GROUP)) {
                    return CheckResult.wrong("It should be printed \"%s\" if the group have not been created yet");
                }

                main.execute("group create BOYS (Elon, Bob, Chuck)");
                String showGroupResult = main.execute("group show BOYS").trim();
                if (!equalsByLines(showGroupResult, "" +
                    "Bob\n" +
                    "Chuck\n" +
                    "Elon")) {
                    return CheckResult.wrong(GROUP_PERSONS_FEEDBACK);
                }
                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute("writeOff");
                main.execute("group create COFFEETEAM (Ann, Bob)");
                main.execute("purchase Bob coffee 10 (COFFEETEAM)");
                String balanceFirst = main.execute("balance close").trim();
                if (!balanceFirst.equals("Ann owes Bob 5.00")) {
                    return CheckResult.wrong("Only Ann owes Bob. Bob should not owe to himself");
                }
                main.execute("repay Ann Bob 5.00");
                String balanceSecond = main.execute("balance close").trim();
                if (!balanceSecond.equals(NO_REPAYMENTS_NEED)) {
                    return CheckResult.wrong(String.format("If everybody owes zero, it should be printed \"%s\"", NO_REPAYMENTS_NEED));
                }
                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute("writeOff");
                main.execute("group create FRIENDS (Ann, Bob, Chuck)");
                main.execute("purchase Elon chocolate 12.50 (FRIENDS)");
                String balanceResult = main.execute("balance close");
                if (!equalsByLines(balanceResult, "" +
                    "Ann owes Elon 4.17\n" +
                    "Bob owes Elon 4.17\n" +
                    "Chuck owes Elon 4.16")) {
                    return CheckResult.wrong("Output should be the same as in example");
                }
                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute("writeOff");
                main.execute("group create BOYS (Elon, Bob, Chuck)");
                main.execute("group create GIRLS (Ann, Diana)");
                main.execute("2020.10.20 purchase Diana flowers 15.65 (BOYS)");
                main.execute("2020.10.21 purchase Chuck chocolate 6.30 (BOYS)");
                main.execute("2020.10.22 purchase Bob icecream 3.99 (GIRLS)");
                String balanceCloseResult = main.execute("balance close");
                if (!equalsByLines(balanceCloseResult, "" +
                    "Ann owes Bob 2.00\n" +
                    "Bob owes Chuck 2.10\n" +
                    "Bob owes Diana 3.23\n" +
                    "Chuck owes Diana 5.22\n" +
                    "Elon owes Chuck 2.10\n" +
                    "Elon owes Diana 5.21"))
                    return CheckResult.wrong("Output should be the same as in example");

                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute("group create GIRLS (Ann, Diana)");
                main.execute("group create TEAM (+Bob, GIRLS, -Frank, Chuck)");
                String groupResult = main.execute("group show TEAM");
                if (!equalsByLines(groupResult, "" +
                    "Ann\n" +
                    "Bob\n" +
                    "Chuck\n" +
                    "Diana")) {
                    return CheckResult.wrong("Program should include Bob, Chuck and persons from GIRLS, also Frank should be excluded");
                }

                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute("writeOff");
                main.execute("group create GIRLS (Ann, Diana)");
                main.execute("group create TEAM (+Bob, GIRLS, -Frank, Chuck)");
                main.execute("2020.10.20 purchase Diana flowers 15.65 (TEAM, Elon, -GIRLS)");
                main.execute("2020.10.21 purchase Elon ChuckBirthdayGift 20.99 (TEAM, -Chuck)");
                String balanceResult = main.execute("balance close");
                if (!equalsByLines(balanceResult, "" +
                    "Ann owes Elon 7.00\n" +
                    "Bob owes Diana 5.22\n" +
                    "Bob owes Elon 7.00\n" +
                    "Chuck owes Diana 5.22\n" +
                    "Diana owes Elon 1.78")) {
                    return CheckResult.wrong("Program should split flowers bill on TEAM with Elon without GIRLS");
                }

                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute("group create SOMEGROUP (Bob)");
                main.execute("group create GIRLS (Ann, Diana)");
                main.execute("group create BOYS (Bob, Chuck, Elon)");
                main.execute("group add SOMEGROUP (GIRLS, Frank)");
                main.execute("group remove SOMEGROUP (-BOYS, Bob, +Frank)");
                String groupResult = main.execute("group show SOMEGROUP");
                if (!equalsByLines(groupResult, "Ann\n" +
                    "Bob\n" +
                    "Diana")) {
                    return CheckResult.wrong("First of all program should collect persons from brackets:" +
                        "At first collect all additions, and then remove all persons to delete." +
                        "eg. group <some group command> GROUP (-BOYS, Bob, +Frank): " +
                        "program should collect Bob and Frank" +
                        "and then remove all persons from BOYS");
                }
                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                List<String> persons = List.of("Gordon", "Bob", "Ann", "Chuck", "Elon", "Diana", "Foxy");

                AtomicInteger counter = new AtomicInteger(0);
                Map<String, Integer> map = persons.stream().sorted()
                    .collect(Collectors.toMap(it -> it, it -> counter.getAndIncrement(),
                        (a, b) -> {
                            throw new UnsupportedOperationException();
                        },
                        LinkedHashMap::new));

                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute(String.format("group create SOMESANTAGROUP (%s)", String.join(",", persons)));
                String secretSantaResult = main.execute("secretSanta SOMESANTAGROUP");

                if (!secretSantaResult.lines().allMatch(it -> it.contains(GIFT_TO))) {
                    return CheckResult.wrong("Each line should contains \" gift to \"");
                }
                List<Integer> sendersList = new ArrayList<>();
                List<Integer> receiversList = new ArrayList<>();
                secretSantaResult.lines().map(String::trim)
                    .map(it -> it.split(GIFT_TO))
                    .forEach(it -> {
                        sendersList.add(map.get(it[0]));
                        receiversList.add(map.get(it[1]));
                    });
                if (sendersList.size() != persons.size() || !isSorted(sendersList)) {
                    return CheckResult.wrong("Program should print persons who will gift to someone in ascending order");
                }
                for (int i = 0; i < sendersList.size(); i++) {
                    if (sendersList.get(i).equals(receiversList.get(i)) && sendersList.size() > 1) {
                        return CheckResult.wrong("Person should not gift a present to himself (in groups larger than 1)");
                    }
                    Integer receiverId = receiversList.get(i);
                    if (sendersList.get(receiverId) == i && sendersList.size() > 2) {
                        return CheckResult.wrong("Person should not gift and receive a present from the same other person (in groups larger than 2)");
                    }
                }
                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute("writeOff");
                main.execute("group create TEAM (Bob, Ann, Frank, Chuck, Elon, Diana)");
                main.execute("2020.12.25 cashBack YourCompany secretSantaGift 24.00 (TEAM)");
                String balanceResult = main.execute("2020.12.25 balance close");
                if (!equalsByLines(balanceResult, "" +
                    "YourCompany owes Ann 4.00\n" +
                    "YourCompany owes Bob 4.00\n" +
                    "YourCompany owes Chuck 4.00\n" +
                    "YourCompany owes Diana 4.00\n" +
                    "YourCompany owes Elon 4.00\n" +
                    "YourCompany owes Frank 4.00")) {
                    return CheckResult.wrong("Program should output list of YourCompany owes to everyone in TEAM");
                }
                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                strToLinesTrimmed("" +
                    "writeOff\n" +
                    "group create TEAM (Ann, Bob, Chuck, Diana, Elon, Frank)\n" +
                    "group create CAR (Diana, Elon)\n" +
                    "group create BUS (Ann, Bob, Chuck, Frank)\n" +
                    "purchase Chuck busTickets 5.25 (BUS, -Frank)\n" +
                    "purchase Elon fuel 25 (CAR, Frank)\n" +
                    "purchase Ann chocolate 2.99 (BUS, -Bob, CAR)\n" +
                    "purchase Diana soda 5.45 (TEAM, -Ann, -Chuck)\n" +
                    "purchase Frank bbq 29.90 (TEAM, CAR, BUS, -Frank, -Bob)\n" +
                    "cashBack YourCompany party 12 (TEAM, BUS)\n" +
                    "cashBack YourCompany tickets 3.50 (BUS)\n" +
                    "borrow Frank Bob 10\n" +
                    "repay Chuck Diana 20")
                    .forEach(main::execute);
                String balanceResult = main.execute("balance close");
                if (!equalsByLines(balanceResult, "Ann owes Chuck 1.15\n" +
                    "Ann owes Frank 6.89\n" +
                    "Bob owes Chuck 1.75\n" +
                    "Bob owes Diana 1.37\n" +
                    "Chuck owes Frank 7.48\n" +
                    "Diana owes Ann 0.60\n" +
                    "Diana owes Chuck 20.00\n" +
                    "Diana owes Elon 6.98\n" +
                    "Diana owes Frank 6.11\n" +
                    "Elon owes Ann 0.60\n" +
                    "Frank owes Bob 10.00\n" +
                    "Frank owes Elon 0.86\n" +
                    "YourCompany owes Ann 2.88\n" +
                    "YourCompany owes Bob 2.88\n" +
                    "YourCompany owes Chuck 2.87\n" +
                    "YourCompany owes Diana 2.00\n" +
                    "YourCompany owes Elon 2.00\n" +
                    "YourCompany owes Frank 2.87")) {
                    return CheckResult.wrong(WRONG_CALCULATIONS);
                }
                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute("writeOff");
                main.execute("group create BOBTEAM (Frank, Bob)");
                main.execute("purchase Ann coffee 12.00 (Chuck, Ann, Bob)");
                {
                    String balanceResult = main.execute("balance close (Bob, Ann)");
                    if (!equalsByLines(balanceResult, "Bob owes Ann 4.00")) {
                        return CheckResult.wrong(FILTERED_BALANCE_FEEDBACK);
                    }
                }
                {
                    String balanceResult = main.execute("balance close (-Bob, BOBTEAM)");
                    if (!equalsByLines(balanceResult, NO_REPAYMENTS_NEED)) {
                        return CheckResult.wrong(String.format("Program should output \"%s\" if no one person in filter have owes", NO_REPAYMENTS_NEED));
                    }
                }
                main.execute("exit");
                return CheckResult.correct();
            }),

            new TestCase().setDynamicTesting(() -> {
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                strToLinesTrimmed("" +
                    "writeOff\n" +
                    "borrow Bob Chuck 100\n" +
                    "borrow Chuck Diana 100\n" +
                    "borrow Chuck Bob 30\n" +
                    "borrow Diana Bob 100")
                    .forEach(main::execute);
                String balancePerfectResult = main.execute("balancePerfect close");
                if (getLinesCount(balancePerfectResult) >= 3) {
                    return CheckResult.wrong(BALANCE_PERFECT_FEEDBACK);
                }
                if (equalsByLines(balancePerfectResult, "Chuck owes Bob 30.00") ||
                    equalsByLines(balancePerfectResult, "" +
                        "Chuck owes Diana 30.00\n" +
                        "Diana owes Bob 30.00")) {
                    main.execute("exit");
                    return CheckResult.correct();
                }
                return CheckResult.wrong(BALANCE_PERFECT_FEEDBACK);
            }),

            new TestCase().setDynamicTesting(() -> {
                Random random = new Random();
                Random RANDOM_DETERM = new Random(42);
                List<String> persons = List.of("Andy", "Bobby", "Claire", "Diana", "Eagle", "Forb");
                String keyPerson = persons.get(random.nextInt(persons.size()));
                BigDecimal keyBalanceBorrow = BigDecimal.ZERO;
                BigDecimal keyBalanceRepay = BigDecimal.ZERO;
                TestedProgram main = new TestedProgram();
                ((MainMethodExecutor) main.getProgramExecutor()).setUseSeparateClassLoader(false);
                main.start();
                main.execute("writeOff");
                for (int i = 0; i < 100; i++) {
                    String personFrom = persons.get(RANDOM_DETERM.nextInt(persons.size()));
                    String personTo = persons.get(RANDOM_DETERM.nextInt(persons.size()));
                    if (personFrom.equalsIgnoreCase(personTo)) {
                        continue;
                    }
                    Commands command;
                    BigDecimal amount = new BigDecimal(String.format("%d.%d", RANDOM_DETERM.nextInt(200), random.nextInt(99)));
                    if (RANDOM_DETERM.nextBoolean()) {
                        command = Commands.borrow;
                        if (personFrom.equals(keyPerson)) {
                            keyBalanceBorrow = keyBalanceBorrow.add(amount);
                        }
                        if (personTo.equals(keyPerson)) {
                            keyBalanceBorrow = keyBalanceBorrow.subtract(amount);
                        }
                    } else {
                        command = Commands.repay;
                        if (personFrom.equals(keyPerson)) {
                            keyBalanceRepay = keyBalanceRepay.add(amount);
                        }
                        if (personTo.equals(keyPerson)) {
                            keyBalanceRepay = keyBalanceRepay.subtract(amount);
                        }
                    }
                    String line = String.format("%s %s %s %s", command, personFrom, personTo, amount);
                    main.execute(line);
                }
                String balanceResult = main.execute("balance close");
                boolean isOkBalance = isBalanceResultOk(keyPerson, balanceResult, keyBalanceBorrow.subtract(keyBalanceRepay));
                if (!isOkBalance) {
                    return CheckResult.wrong("Command balance: " + WRONG_CALCULATIONS);
                }
                String balancePerfectResult = main.execute("balancePerfect close");
                boolean isOkBalancePerfect = isBalanceResultOk(keyPerson, balancePerfectResult, keyBalanceBorrow.subtract(keyBalanceRepay));
                if (!isOkBalancePerfect) {
                    return CheckResult.wrong("Command balancePerfect: " + WRONG_CALCULATIONS);
                }
                if (getLinesCount(balancePerfectResult) >= getLinesCount(balanceResult)) {
                    return CheckResult.wrong(BALANCE_PERFECT_FEEDBACK);
                }
                main.execute("exit");
                return CheckResult.correct();
            })


        );
    }

    private static long getLinesCount(String balancePerfectResult) {
        return balancePerfectResult.lines()
            .filter(Predicate.not(String::isBlank))
            .count();
    }

    private static boolean isBalanceResultOk(String keyPerson, String result, BigDecimal sumBalance) {
        Optional<BigDecimal> sum = Arrays.stream(result.split("\n"))
            .filter(it -> it.contains(keyPerson))
            .map(it -> {
                String[] split = it.split("\\s+");
                Character sign = it.startsWith(keyPerson) ? '+' : '-';
                return sign + split[split.length - 1];
            })
            .map(BigDecimal::new)
            .reduce(BigDecimal::add);
        return sumBalance.compareTo(sum.orElse(BigDecimal.ZERO)) == 0;
    }


    private <T extends Comparable<T>> boolean isSorted(List<T> list) {
        ArrayList<T> sorted = new ArrayList<>(list);
        return sorted.equals(list);
    }

    private CheckResult checkHelpCommand(String reply, String attach) {
        String[] replyArr = reply.split("\n");
        List<String> commandList = getCommandList();
        if (replyArr.length != commandList.size()) {
            return CheckResult.wrong(HELP_ERROR);
        }
        for (int i = 0; i < replyArr.length; i++) {
            if (!replyArr[i].toLowerCase().startsWith(commandList.get(i).toLowerCase())) {
                return CheckResult.wrong(HELP_ERROR);
            }
        }
        return CheckResult.correct();
    }

    private CheckResult checkUnknownCommand(String reply, String attach) {
        try {
            reply = reply.trim();
            Commands command = Commands.valueOf(reply);
        } catch (IllegalArgumentException e) {
            if (!reply.toLowerCase().startsWith(UNKNOWN_COMMAND.toLowerCase())) {
                return CheckResult.wrong(String.format("For unknown command output should starts with: %s", UNKNOWN_COMMAND));
            }
        }
        return CheckResult.correct();
    }

    private List<String> getCommandList() {
        return Arrays.stream(Commands.values())
            .map(Enum::toString)
            .sorted().collect(Collectors.toList());
    }

    private String concatLines(List<String> strings) {
        return String.join("\n", strings);
    }

    private String concatLines(String... strings) {
        return String.join("\n", strings);
    }

    private static boolean equalsByLines(String sample, String linesStr) {
        List<String> sampleLines = strToLinesTrimmed(sample);
        List<String> lines = strToLinesTrimmed(linesStr);
        return sampleLines.equals(lines);
    }

    private static List<String> strToLinesTrimmed(String sample) {
        return sample.lines().map(String::trim).collect(Collectors.toList());
    }
}