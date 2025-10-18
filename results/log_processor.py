import sys
import re
import csv

REGEX_PATTERN = r"INFO: \[(?P<alias>(?:\w|-)+)\] Instance (?P<instance>(?:\w|-|\/|\.)+) - maxVal = Solution: cost=\[-(?P<objective>(?:\d|\.)+)"

def main() -> None:
    if len(sys.argv) != 2:
        print("Usage: python log_processor.py <log_file_path>")
        sys.exit(1)
    log_file_path = sys.argv[1]
    instances = {}
    with open(log_file_path, 'r') as file:
        for line in file:
            match = re.search(REGEX_PATTERN, line)
            if match:
                alias = match.group("alias")
                instance = int(match.group("instance").split("/")[-1].split(".")[0].split("_")[-1]) + 1
                objective = match.group("objective")
                if instance not in instances:
                    instances[instance] = {}
                instances[instance][alias] = objective
    order = ["PADRAO", "PADRAO_POP", "PADRAO_MUT", "PADRAO_EVOL1", "PADRAO_EVOL2"]
    with open('summary.csv', 'w+', newline='') as csvfile:
        fieldnames = ['Instance'] + order
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        for instance in sorted(instances.keys()):
            row = {'Instance': instance}
            for alias in order:
                row[alias] = instances[instance].get(alias, 'N/A')
            writer.writerow(row)    

if __name__ == "__main__":
    main()