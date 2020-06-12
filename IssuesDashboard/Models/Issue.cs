using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace IssuesDashboar.Models
{
    public class Issue
    {
        public int Id { get; set; }
        public string Component { get; set; } = "";
        public int Line { get; set; }
        public string Message { get; set; } = "";
        public string Date { get; set; } = "";
    }
}
