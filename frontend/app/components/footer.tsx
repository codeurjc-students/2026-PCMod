import { Envelope, Youtube } from 'react-bootstrap-icons';

export default function Footer() {
  return <>
    <footer id="footer" className="footer sticky-bottom">
      <div className="social-container">

        <div className="col-1 text-center">
          <a href="mailto:...." className="social-link" title="Email">
            <Envelope />
            Contact us via email!
          </a>
        </div>

        <div className="col-1 text-center">
          <a href="https://youtube.com/@...." target="_blank" className="social-link" title="YouTube">
            <Youtube />
            <span>Follow us on YouTube!</span>
          </a>
        </div>
      </div>

      <div className="footer-bottom">
        <div className="footer-bottom-content">
          <p className="mb-0"><span className="sitename">PCMod</span></p>
        </div>
      </div>

    </footer>
  </>
}