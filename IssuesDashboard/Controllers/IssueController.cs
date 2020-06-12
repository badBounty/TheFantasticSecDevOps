using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using IssuesDashboar.Models;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Nest;

namespace IssuesDashboar.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class IssueController : Controller
    {

        private Uri Node { get; set; }
        private ConnectionSettings Settings { get; set; }
        private ElasticClient Client { get; set; }


        public IssueController()
        {
            Node = new Uri("http://192.168.0.23:9200");
            Settings = new ConnectionSettings(Node);
            Client = new ElasticClient(Settings);
        }

        private IEnumerable<Issue> List()
        {
            List<Issue> issues = new List<Issue>();
            var scroll = Client.Search<Issue>(s => s
           .Index("issuesindex").From(0).Size(1).MatchAll().Scroll("10s")
           );

            issues.AddRange(scroll.Documents);
            var results = Client.Scroll<Issue>("10s", scroll.ScrollId);
            while (results.Documents.Any())
            {
                issues.AddRange(results.Documents);
                results = Client.Scroll<Issue>("10s", results.ScrollId);
            }
            return issues.OrderBy(i => i.Id);
        }

        // GET: api/Issue
        [HttpGet]
        public ActionResult<IEnumerable<Issue>> Get()
        {
            return Ok(List());
        }



        // POST: api/Issue
        [HttpPost]
        public ActionResult<string> Post(Issue Issue)
        {
            var issues = List();

            Issue.Id = issues.Count() + 1;

            if (Issue.Date.Length < 1)
            {
                Issue.Date = DateTime.Today.ToString("yyyy-MM-dd");
            }

            var res = Client.Index(Issue, idx => idx.Index("issuesindex"));
            Task.Delay(6000);
            return Ok(Issue);

        }
    }
}